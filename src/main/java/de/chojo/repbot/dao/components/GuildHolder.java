/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.components;

import net.dv8tion.jda.api.entities.Guild;

/**
 * Interface representing a holder for a Guild.
 */
public interface GuildHolder {

    /**
     * Gets the Guild associated with this holder.
     *
     * @return the Guild
     */
    Guild guild();

    /**
     * Gets the ID of the Guild associated with this holder.
     *
     * @return the Guild ID
     */
    long guildId();
}
