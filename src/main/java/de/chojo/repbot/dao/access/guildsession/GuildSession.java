/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guildsession;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.web.pojo.GuildSessionPOJO;
import de.chojo.repbot.web.validation.GuildValidator;
import de.chojo.repbot.web.validation.PremiumValidator;
import io.javalin.http.UnauthorizedResponse;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.Permission.ADMINISTRATOR;
import static net.dv8tion.jda.api.Permission.BAN_MEMBERS;
import static net.dv8tion.jda.api.Permission.KICK_MEMBERS;
import static net.dv8tion.jda.api.Permission.MANAGE_CHANNEL;
import static net.dv8tion.jda.api.Permission.MANAGE_ROLES;
import static net.dv8tion.jda.api.Permission.MANAGE_SERVER;

public class GuildSession {
    private static final List<Permission> PRIVILEGED_PERMISSIONS =
            List.of(ADMINISTRATOR, MANAGE_SERVER, MANAGE_ROLES, KICK_MEMBERS, BAN_MEMBERS, MANAGE_CHANNEL);
    private final Configuration configuration;
    private final ShardManager shardManager;
    private final GuildRepository guildRepository;
    private final GuildSessionMeta meta;
    private PremiumValidator premiumValidator;
    private GuildValidator guildValidator;

    public GuildSession(
            Configuration configuration,
            ShardManager shardManager,
            GuildRepository guildRepository,
            GuildSessionMeta meta) {
        this.configuration = configuration;
        this.shardManager = shardManager;
        this.guildRepository = guildRepository;
        this.meta = meta;
    }

    public Guild guild() {
        return shardManager.getGuildById(meta.guildId());
    }

    public RepGuild repGuild() {
        return guildRepository.guild(guild());
    }

    @NotNull
    public GuildSessionPOJO sessionData() {
        return GuildSessionPOJO.generate(guild(), guildRepository, shardManager);
    }

    public ShardManager shardManager() {
        return shardManager;
    }

    public PremiumValidator premiumValidator() {
        if (premiumValidator == null) {
            premiumValidator = new PremiumValidator(repGuild(), shardManager);
        }
        return premiumValidator;
    }

    public GuildValidator guildValidator() {
        if (guildValidator == null) {
            guildValidator = new GuildValidator(this);
        }
        return guildValidator;
    }

    public long guildId() {
        return meta.guildId();
    }

    public long memberId() {
        return meta.memberId();
    }

    public String sessionUrl() {
        return pathUrl("");
    }

    public String setupUrl() {
        return pathUrl("setup");
    }

    public GuildSessionMeta meta() {
        return meta;
    }

    public String pathUrl(String path) {
        String url = "%s/%s?token=%s".formatted(configuration.api().url(), path, meta.token());
        return repGuild()
                .settings()
                .general()
                .language()
                .map(lang -> "%s&lang=%s".formatted(url, lang.getLocale()))
                .orElse(url);
    }

    public Configuration configuration() {
        return configuration;
    }

    /**
     * Records a change of a guild setting.
     *
     * @param settingsKey A unique key identifying the setting.
     * @param oldValue    the old value of the setting before the change.
     * @param newValue    the new value of the setting after the change.
     */
    public void recordChange(String settingsKey, Object oldValue, Object newValue) {
        meta.recordChange(settingsKey, oldValue, newValue);
    }

    /**
     * Validates that the member owning this session is still a member of the guild.
     */
    public void validate() {
        if (configuration.baseSettings().isOwner(memberId())) return;
        try {
            Member member = guild().retrieveMemberById(memberId()).complete();
            if (PRIVILEGED_PERMISSIONS.stream().anyMatch(member::hasPermission)) return;
            throw new UnauthorizedResponse("User does not have any of the required permissions: %s"
                    .formatted(PRIVILEGED_PERMISSIONS.stream()
                            .map(Permission::getName)
                            .collect(Collectors.joining(", "))));
        } catch (Exception e) {
            throw new UnauthorizedResponse("User is not a member of this guild anymore.");
        }
    }
}
