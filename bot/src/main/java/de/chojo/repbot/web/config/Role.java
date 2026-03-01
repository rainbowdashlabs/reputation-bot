/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.config;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    /**
     * The public/unauthorized role.
     */
    ANYONE,
    /**
     * The user role. The level a logged-in and authenticated user has.
     */
    USER,
    /**
     * The user of a guild that the bot is in without any additional rights.
     */
    GUILD_USER,
    /**
     * The user of a guild that the bot is in with the administrator rights, allowing to modify bot settings.
     */
    GUILD_ADMIN
}
