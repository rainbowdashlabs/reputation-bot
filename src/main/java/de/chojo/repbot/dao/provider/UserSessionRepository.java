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

    /**
     * Finds a session that was used within the last hours.
     *
     * @param token      the session token
     * @param validHours sessions not used for this many hours are expired
     * @return the session, or empty if it does not exist or has expired
     */
    public Optional<UserSessionMeta> byToken(String token, int validHours) {
        return query("SELECT * FROM user_session WHERE token = ? AND last_used >= now() - make_interval(hours => ?)")
                .single(call().bind(token).bind(validHours))
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

    /**
     * Deletes all sessions that were not used within the last hours.
     *
     * @param validHours sessions not used for this many hours are deleted
     * @return the number of deleted sessions
     */
    public int deleteExpired(int validHours) {
        return query("DELETE FROM user_session WHERE last_used < now() - make_interval(hours => ?)")
                .single(call().bind(validHours))
                .delete()
                .rows();
    }
}
