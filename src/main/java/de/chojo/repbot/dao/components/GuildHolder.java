/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.components;

import net.dv8tion.jda.api.entities.Guild;

public interface GuildHolder {
    default Guild guild() {
        return guildHolder().guild();
    }

    default long guildId() {
        return guildHolder().guildId();
    }

    GuildHolder guildHolder();
}
