/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.user;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;
import java.time.Instant;

import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public record UserToken(long userId, String accessToken, String refreshToken, Instant expiry) {
    @MappingProvider({"user_id", "access_token", "refresh_token", "expiry"})
    public UserToken(Row row) throws SQLException {
        this(
                row.getLong("user_id"),
                row.getString("access_token"),
                row.getString("refresh_token"),
                row.get("expiry", INSTANT_TIMESTAMP));
    }
}
