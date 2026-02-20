/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.services;

import de.chojo.repbot.web.pojo.guild.MemberPOJO;
import de.chojo.repbot.web.pojo.session.GuildSessionData;
import de.chojo.repbot.web.pojo.session.UserSessionPOJO;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public record UserSession(
        long userId,
        String token,
        Map<String, GuildSessionData> guilds,
        MemberPOJO member,
        Instant created,
        boolean isBotOwner) {
    public UserSessionPOJO toPOJO() {
        return new UserSessionPOJO(token, Collections.unmodifiableMap(guilds), member, isBotOwner);
    }
}
