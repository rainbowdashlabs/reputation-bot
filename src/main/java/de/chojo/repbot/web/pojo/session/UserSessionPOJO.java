/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.session;

import de.chojo.repbot.web.pojo.guild.MemberPOJO;

import java.util.Map;

public record UserSessionPOJO(
        String token, Map<String, GuildSessionData> guilds, MemberPOJO member, boolean isBotOwner) {}
