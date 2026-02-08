/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.settings.sub.AbuseProtection;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.snapshots.ReputationRank;
import de.chojo.repbot.service.debugService.DebugResult;
import de.chojo.repbot.service.debugService.GeneralPermissions;
import de.chojo.repbot.service.debugService.MissingPermissions;
import de.chojo.repbot.service.debugService.PermissionScope;
import de.chojo.repbot.service.debugService.RankProblem;
import de.chojo.repbot.service.debugService.ReputationChannelProblem;
import de.chojo.repbot.service.debugService.SimpleProblems;
import de.chojo.repbot.service.debugService.SimpleWarning;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.SelfMember;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static de.chojo.repbot.service.debugService.RanksProblemType.CANT_ASSIGN_ROLE;
import static de.chojo.repbot.service.debugService.RanksProblemType.MISSING_ROLE;
import static de.chojo.repbot.service.debugService.ReputationChannelProblemType.MISSING_CATEGORY;
import static de.chojo.repbot.service.debugService.ReputationChannelProblemType.MISSING_CHANNEL;
import static de.chojo.repbot.service.debugService.ReputationChannelProblemType.NOT_TEXT_TYPE;
import static net.dv8tion.jda.api.Permission.BYPASS_SLOWMODE;
import static net.dv8tion.jda.api.Permission.MESSAGE_ADD_REACTION;
import static net.dv8tion.jda.api.Permission.MESSAGE_EMBED_LINKS;
import static net.dv8tion.jda.api.Permission.MESSAGE_EXT_EMOJI;
import static net.dv8tion.jda.api.Permission.MESSAGE_HISTORY;
import static net.dv8tion.jda.api.Permission.MESSAGE_SEND;
import static net.dv8tion.jda.api.Permission.MESSAGE_SEND_IN_THREADS;
import static net.dv8tion.jda.api.Permission.VIEW_CHANNEL;

public class DebugService {
    private final GuildRepository guildRepository;
    private static final List<Permission> DEFAULT_REPUTATION_CHANNEL_PERMISSION = List.of(
            VIEW_CHANNEL,
            MESSAGE_SEND,
            MESSAGE_EMBED_LINKS,
            MESSAGE_HISTORY,
            MESSAGE_ADD_REACTION,
            MESSAGE_SEND_IN_THREADS,
            MESSAGE_EXT_EMOJI);

    public DebugService(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    public DebugResult debug(Guild guild) {
        guildRepository.guild(guild);

        SelfMember self = guild.getSelfMember();
        RepGuild repGuild = guildRepository.guild(guild);
        Settings settings = repGuild.settings();

        DebugResult result = new DebugResult();

        for (GeneralPermissions value : GeneralPermissions.values()) {
            if (!value.check(self::hasPermission)) {
                result.addMissingGlobalPermission(value.permission());
            }
        }

        checkChannel(
                guild,
                self,
                result,
                settings.general().systemChannel(),
                SimpleProblems.SYSTEM_CHANNEL_NOT_DEFINED,
                SimpleProblems.SYSTEM_CHANNEL_NOT_FOUND,
                result.getMissingGlobalPermissions());

        for (ReputationRank reputationRank : settings.ranks().ranks().stream()
                .sorted(ReputationRank::compareTo)
                .toList()) {
            Optional<Role> optRole = reputationRank.role();

            if (optRole.isEmpty()) {
                result.addRankProblem(new RankProblem(reputationRank.roleId(), List.of(MISSING_ROLE)));
                continue;
            }

            Role role = optRole.get();
            if (!self.canInteract(role)) {
                result.addRankProblem(new RankProblem(reputationRank.roleId(), List.of(CANT_ASSIGN_ROLE)));
            }
        }

        for (long channelId : settings.thanking().channels().channelIds()) {
            GuildChannel channel = guild.getGuildChannelById(channelId);
            if (channel == null) {
                result.addReputationChannelProblem(new ReputationChannelProblem(channelId, MISSING_CHANNEL));
                continue;
            }
            if (!(channel instanceof GuildMessageChannel) && !(channel instanceof ForumChannel)) {
                result.addReputationChannelProblem(new ReputationChannelProblem(channelId, NOT_TEXT_TYPE));
                continue;
            }
            List<Permission> permissions = new LinkedList<>(DEFAULT_REPUTATION_CHANNEL_PERMISSION);
            if (channel instanceof ISlowmodeChannel) {
                permissions.add(BYPASS_SLOWMODE);
            }
            List<Permission> missing = checkPermission(
                    p -> self.hasPermission(channel, p), permissions, result.getMissingGlobalPermissions());
            if (!missing.isEmpty()) {
                result.addMissingPermissions(new MissingPermissions(PermissionScope.CHANNEL, channelId, missing));
            }
        }

        for (Long categoryId : settings.thanking().channels().categoryIds()) {
            Category category = guild.getCategoryById(categoryId);
            if (category == null) {
                result.addReputationChannelProblem(new ReputationChannelProblem(categoryId, MISSING_CATEGORY));
                continue;
            }
            List<Permission> missing = checkPermission(
                    p -> self.hasPermission(category, p),
                    DEFAULT_REPUTATION_CHANNEL_PERMISSION,
                    result.getMissingGlobalPermissions());
            if (!missing.isEmpty()) {
                result.addMissingPermissions(new MissingPermissions(PermissionScope.CATEGORY, categoryId, missing));
            }

            for (GuildChannel channel : category.getChannels()) {
                List<PermissionOverride> permissionOverrides =
                        channel.getPermissionContainer().getPermissionOverrides();
                if (permissionOverrides.isEmpty()) continue;
                List<Permission> missingPermissions = checkPermission(
                        self::hasPermission,
                        DEFAULT_REPUTATION_CHANNEL_PERMISSION,
                        result.getMissingGlobalPermissions());
                if (!missingPermissions.isEmpty()) {
                    result.addMissingPermissions(
                            new MissingPermissions(PermissionScope.CHANNEL, channel.getIdLong(), missingPermissions));
                }
            }
        }

        if (settings.thanking().channels().categoryIds().isEmpty()
                && settings.thanking().channels().channelIds().isEmpty()
                && settings.thanking().channels().isWhitelist()) {
            result.addSimpleProblem(SimpleProblems.NO_REPUTATION_CHANNEL_DEFINED);
        }

        if (settings.announcements().active() && !settings.announcements().sameChannel()) {
            checkChannel(
                    guild,
                    self,
                    result,
                    settings.announcements().channelId(),
                    SimpleProblems.NO_ANNOUNCEMENT_CHANNEL_DEFINED,
                    SimpleProblems.ANNOUNCEMENT_CHANNEL_NOT_FOUND,
                    result.getMissingGlobalPermissions());
        }

        if (settings.autopost().active()) {
            checkChannel(
                    guild,
                    self,
                    result,
                    settings.autopost().channelId(),
                    SimpleProblems.NO_AUTOPOST_CHANNEL_DEFINED,
                    SimpleProblems.AUTOPOST_CANNEL_NOT_FOUND,
                    result.getMissingGlobalPermissions());
        }

        if (settings.logChannel().active()) {
            checkChannel(
                    guild,
                    self,
                    result,
                    settings.logChannel().channelId(),
                    SimpleProblems.NO_LOG_CHANNEL_DEFINED,
                    SimpleProblems.LOG_CHANNEL_NOT_FOUND,
                    result.getMissingGlobalPermissions());
        }

        if (settings.thanking().thankwords().words().isEmpty()) {
            result.addSimpleProblem(SimpleProblems.NO_THANKWORDS_DEFINED);
        }

        AbuseProtection abuseProtection = settings.abuseProtection();
        if (abuseProtection.maxMessageAge() < 30) {
            result.addSimpleWarning(SimpleWarning.MAX_MESSAGE_AGE_LOW);
        }

        return result;
    }

    private void checkChannel(
            Guild guild,
            SelfMember self,
            DebugResult result,
            long channelId,
            SimpleProblems notSet,
            SimpleProblems notFound,
            List<Permission> globalMissingPermissions) {
        TextChannel channel = guild.getTextChannelById(channelId);
        if (channelId == 0) {
            result.addSimpleProblem(notSet);
        } else if (channel == null) {
            result.addSimpleProblem(notFound);
        } else {
            List<Permission> missing = checkPermission(
                    p -> self.hasPermission(channel, p),
                    List.of(VIEW_CHANNEL, MESSAGE_SEND, MESSAGE_EMBED_LINKS),
                    globalMissingPermissions);
            if (!missing.isEmpty()) {
                result.addMissingPermissions(new MissingPermissions(PermissionScope.CHANNEL, channelId, missing));
            }
        }
    }

    private List<Permission> checkPermission(
            Function<Permission, Boolean> permissionCheck,
            List<Permission> permissions,
            List<Permission> globalMissingPermissions) {
        return permissions.stream()
                .filter(p -> !permissionCheck.apply(p))
                .filter(p -> !globalMissingPermissions.contains(p))
                .toList();
    }
}
