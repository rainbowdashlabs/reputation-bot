/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.session;

import de.chojo.repbot.web.config.Role;

public record GuildSessionData(
        Role accessLevel,
        String id,
        String name,
        String icon,
        String permissions,
        String permissionsOld,
        boolean owner) {}
