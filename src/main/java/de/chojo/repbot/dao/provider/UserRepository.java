/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.provider;

import de.chojo.repbot.dao.access.user.UserToken;

import java.time.Instant;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class UserRepository {

    public Optional<UserToken> token(long userId) {
        return query("SELECT * FROM user_token WHERE user_id = ?")
                .single(call().bind(userId))
                .mapAs(UserToken.class)
                .first();
    }

    public void updateToken(long userId, String accessToken, String refreshToken, Instant expiry) {
        query("""
                INSERT INTO user_token (user_id, access_token, refresh_token, expiry)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (user_id) DO UPDATE SET
                    access_token = excluded.access_token,
                    refresh_token = excluded.refresh_token,
                    expiry = excluded.expiry
                """)
                .single(call().bind(userId).bind(accessToken).bind(refreshToken).bind(expiry, INSTANT_TIMESTAMP))
                .insert();
    }
}
