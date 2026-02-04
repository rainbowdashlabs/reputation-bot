/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.config;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    ANYONE, GUILD_USER
}
