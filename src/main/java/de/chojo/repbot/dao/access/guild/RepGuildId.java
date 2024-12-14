/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.config.Configuration;

import javax.sql.DataSource;

/**
 * Represents a guild with a specific ID in the reputation bot.
 */
public class RepGuildId extends RepGuild {
    private final long guildId;

    /**
     * Constructs a RepGuildId with the specified guild ID and configuration.
     *
     * @param guildId the ID of the guild
     * @param configuration the configuration
     */
    public RepGuildId(long guildId, Configuration configuration) {
        super(null, configuration);
        this.guildId = guildId;
    }

    /**
     * Returns the ID of the guild.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return guildId;
    }
}
