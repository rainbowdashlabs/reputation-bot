/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import net.dv8tion.jda.api.entities.Guild;

/**
 * Utility class for guild-related operations.
 */
public final class Guilds {

    /**
     * Private constructor to prevent instantiation.
     * Throws an UnsupportedOperationException if called.
     */
    private Guilds() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Generates a pretty name for the guild, including its name and ID.
     *
     * @param guild the Guild instance
     * @return a formatted string containing the guild's name and ID
     */
    public static String prettyName(Guild guild) {
        return String.format("%s (%s)", guild.getName(), guild.getIdLong());
    }
}
