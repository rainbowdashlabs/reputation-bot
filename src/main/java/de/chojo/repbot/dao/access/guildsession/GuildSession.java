/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guildsession;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.provider.SettingsAuditLogRepository;
import de.chojo.repbot.web.pojo.GuildSessionPOJO;
import de.chojo.repbot.web.validation.GuildValidator;
import de.chojo.repbot.web.validation.PremiumValidator;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

public class GuildSession {
    private final Configuration configuration;
    private final ShardManager shardManager;
    private final GuildRepository guildRepository;
    private final SettingsAuditLogRepository settingsAuditLogRepository;
    private final long guildId;
    private final long userId;
    private PremiumValidator premiumValidator;
    private GuildValidator guildValidator;
    private volatile boolean dirty = true;
    private GuildSessionPOJO cachedSessionData;

    public GuildSession(
            Configuration configuration,
            ShardManager shardManager,
            GuildRepository guildRepository,
            SettingsAuditLogRepository settingsAuditLogRepository,
            long guildId,
            long userId) {
        this.configuration = configuration;
        this.shardManager = shardManager;
        this.guildRepository = guildRepository;
        this.settingsAuditLogRepository = settingsAuditLogRepository;
        this.guildId = guildId;
        this.userId = userId;
    }

    public Guild guild() {
        return shardManager.getGuildById(guildId);
    }

    public RepGuild repGuild() {
        return guildRepository.guild(guild());
    }

    @NotNull
    public GuildSessionPOJO sessionData() {
        if (dirty || cachedSessionData == null) {
            cachedSessionData = GuildSessionPOJO.generate(guild());
            dirty = false;
        }
        return cachedSessionData;
    }

    public void markDirty() {
        this.dirty = true;
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

    public GuildRepository guildRepository() {
        return guildRepository;
    }

    public long guildId() {
        return guildId;
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
        settingsAuditLogRepository.recordChange(guildId, userId, settingsKey, oldValue, newValue);
    }

    /**
     * @deprecated Used to validate if the member is still in the guild and has permissions.
     * This is now handled during session creation and request interception.
     */
    @Deprecated
    public void validate() {
        // No-op for now, as validation is moved to handleAccess
    }
}
