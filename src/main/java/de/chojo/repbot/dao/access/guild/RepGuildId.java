/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.config.Configuration;

public class RepGuildId extends RepGuild {
    private final long guildId;

    public RepGuildId(long guildId, Configuration configuration) {
        super(null, configuration);
        this.guildId = guildId;
    }

    @Override
    public long guildId() {
        return guildId;
    }
}
