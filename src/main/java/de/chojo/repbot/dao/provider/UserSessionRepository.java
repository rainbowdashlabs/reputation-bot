/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.provider;

import de.chojo.repbot.dao.access.session.UserSessionMeta;

import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class UserSessionRepository {

    public Optional<UserSessionMeta> byToken(String token) {
        return query("SELECT * FROM user_session WHERE token = ?")
                .single(call().bind(token))
                .mapAs(UserSessionMeta.class)
                .first();
    }

    public void updateLastUsed(String token) {
        query("UPDATE user_session SET last_used = now() WHERE token = ?")
                .single(call().bind(token))
                .update();
    }

    public void createSession(String token, long userId) {
        query("INSERT INTO user_session (token, user_id) VALUES (?, ?)")
                .single(call().bind(token).bind(userId))
                .insert();
    }

    public void deleteSession(String token) {
        query("DELETE FROM user_session WHERE token = ?")
                .single(call().bind(token))
                .delete();
    }
}
