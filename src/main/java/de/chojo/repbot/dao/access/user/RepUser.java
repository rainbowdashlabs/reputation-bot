/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.user;

import de.chojo.repbot.dao.access.user.sub.UserMails;
import de.chojo.repbot.dao.access.user.sub.UserSettings;
import de.chojo.repbot.dao.access.user.sub.UserToken;

import java.time.Instant;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class RepUser {
    private final long id;
    private UserSettings settings;
    private UserMails mails;

    public RepUser(long id) {
        this.id = id;
    }

    public Optional<UserToken> token() {
        return query("SELECT user_id, access_token, refresh_token, expiry FROM user_token WHERE user_id = ?")
                .single(call().bind(id))
                .mapAs(UserToken.class)
                .first();
    }

    public void updateToken(String accessToken, String refreshToken, Instant expiry) {
        query("""
                INSERT INTO user_token (user_id, access_token, refresh_token, expiry)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (user_id) DO UPDATE SET
                    access_token = excluded.access_token,
                    refresh_token = excluded.refresh_token,
                    expiry = excluded.expiry
                """)
                .single(call().bind(id).bind(accessToken).bind(refreshToken).bind(expiry, INSTANT_TIMESTAMP))
                .insert();
    }

    public UserSettings settings() {
        if (settings == null) {
            settings = query("""
                    SELECT id, vote_guild FROM user_settings WHERE id = ?
                    """)
                    .single(call().bind(id))
                    .mapAs(UserSettings.class)
                    .first()
                    .orElseGet(() -> new UserSettings(id, 0));
        }
        return settings;
    }

    public UserMails mails() {
        if (mails == null) {
            mails = new UserMails(this);
        }
        return mails;
    }

    public long id() {
        return id;
    }
}
