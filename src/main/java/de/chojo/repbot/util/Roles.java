/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import net.dv8tion.jda.api.entities.Role;

/**
 * Utility class for handling roles.
 */
public final class Roles {
    /**
     * Private constructor to prevent instantiation.
     * Throws UnsupportedOperationException if called.
     */
    private Roles() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Returns a pretty string representation of the role.
     *
     * @param role the role to format
     * @return the formatted string representation of the role
     */
    public static String prettyName(Role role) {
        return String.format("%s (%s)", role.getName(), role.getIdLong());
    }
}
