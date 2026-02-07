/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static de.chojo.jdautil.localization.util.Format.BOLD;
import static de.chojo.jdautil.localization.util.Replacement.create;
import static de.chojo.jdautil.localization.util.Replacement.createMention;

public final class PermissionErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(PermissionErrorHandler.class);
    private static final List<Permission> NON_ACCESS_PERMISSIONS =
            List.of(Permission.MESSAGE_SEND, Permission.MESSAGE_SEND_IN_THREADS, Permission.VIEW_CHANNEL);
    private static final Cache<ErrorKey, Instant> lastSent =
            CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    private PermissionErrorHandler() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static boolean handle(
            RepGuild repGuild,
            LocalizationContext localizer,
            GuildChannel channel,
            Configuration configuration,
            Permission... permissions) {
        // Ignore botlists
        Guild guild = repGuild.guild();
        if (configuration.botlist().isBotlistGuild(guild.getIdLong())) return false;
        // Piece together the error message
        List<String> errorMessages = new ArrayList<>();
        for (Permission permission : permissions) {
            errorMessages.add(
                    localizer.localize("error.missingPermission", create("PERM", permission.getName(), BOLD)));
        }

        ErrorKey key;
        if (guild.getSelfMember().hasPermission(permissions)) {
            key = new ErrorKey(guild, channel, List.of(permissions));
            errorMessages.add(localizer.localize("error.missingPermissionChannel", createMention("CHANNEL", channel)));
        } else {
            key = new ErrorKey(guild, null, List.of(permissions));
            errorMessages.add(localizer.localize("error.missingPermissionGuild"));
        }

        if (lastSent.getIfPresent(key) != null) return false;
        lastSent.put(key, Instant.now());

        // First try the system channel
        TextChannel systemChannel =
                guild.getTextChannelById(repGuild.settings().general().systemChannel());

        if (systemChannel != null) {
            try {
                systemChannel.sendMessage(String.join("\n", errorMessages)).complete();
                return true;
            } catch (Exception e) {
                repGuild.settings().general().systemChannel(0);
            }
        }

        errorMessages.add(localizer.localize("error.nosystemchannel"));

        String joinedError = String.join("\n", errorMessages);

        if (sendGuildOwner(guild, joinedError)) return true;
        if (sendGuildAdmin(guild, joinedError)) return true;

        // Send directly to channel
        if (Arrays.stream(permissions).noneMatch(NON_ACCESS_PERMISSIONS::contains)
                && channel instanceof MessageChannel messageChannel) {
            messageChannel.sendMessage(joinedError).complete();
            return true;
        }
        return false;
    }

    private static boolean sendGuildOwner(Guild guild, String message) {
        try {
            return sendUser(guild.retrieveOwner().complete().getUser(), message);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean sendGuildAdmin(Guild guild, String message) {
        List<Member> administrators = guild.getRoles().stream()
                .filter(r -> r.hasPermission(Permission.ADMINISTRATOR))
                .map(guild::getMembersWithRoles)
                .flatMap(List::stream)
                .distinct()
                .toList();
        for (Member administrator : administrators) {
            if (sendUser(administrator.getUser(), message)) return true;
        }
        return false;
    }

    private static boolean sendUser(User user, String message) {
        try {
            PrivateChannel privateChannel = user.openPrivateChannel().complete();
            privateChannel.sendMessage(message).complete();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void handle(
            GuildRepository guildRepository,
            InsufficientPermissionException permissionException,
            ShardManager shardManager,
            LocalizationContext localizer,
            Configuration configuration) {
        var permission = permissionException.getPermission();
        var guildById = shardManager.getGuildById(permissionException.getGuildId());
        var channel = (TextChannel) permissionException.getChannel(guildById.getJDA());
        if (channel == null) return;
        handle(guildRepository.guild(guildById), localizer, channel, configuration, permission);
    }

    /**
     * Assert that the user has permissions.
     *
     * @param channel     channel to check
     * @param permissions permissions to check
     * @throws InsufficientPermissionException when the bot user doesn't have a permission
     */
    private static List<Permission> assertGuildChannelPermissions(
            GuildMessageChannel channel, Permission... permissions) throws InsufficientPermissionException {
        var self = channel.getGuild().getSelfMember();
        return Arrays.stream(permissions)
                .filter(permission -> !self.hasPermission(channel, permission))
                .toList();
    }

    public static void assertGuildPermissions(Guild guild, Permission... permissions)
            throws InsufficientPermissionException {
        var self = guild.getSelfMember();
        for (var permission : permissions) {
            if (!self.hasPermission(permission)) {
                throw new InsufficientPermissionException(guild, permission);
            }
        }
    }

    /**
     * Checks if the self user has the permissions in this channel and sends a permission error if one is missing.
     *
     * @param channel       channel to check
     * @param localizer     localizer
     * @param configuration configuration
     * @param permissions   permissions to check
     * @return true if a permission was missing and a message was sent
     */
    public static boolean assertAndHandle(
            RepGuild repGuild,
            GuildMessageChannel channel,
            LocalizationContext localizer,
            Configuration configuration,
            Permission... permissions) {
        List<Permission> missing = assertGuildChannelPermissions(channel, permissions);
        if (missing.isEmpty()) return false;
        return handle(repGuild, localizer, channel, configuration, missing.toArray(Permission[]::new));
    }

    private record ErrorKey(Guild guild, @Nullable Channel channel, List<Permission> permissions) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ErrorKey errorKey = (ErrorKey) o;
            return guild.getIdLong() == errorKey.guild.getIdLong()
                    && Objects.equals(channel, errorKey.channel)
                    && permissions.equals(errorKey.permissions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(guild, channel, permissions);
        }
    }
}
