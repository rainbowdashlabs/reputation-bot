/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.session;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;
import java.time.Instant;

import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public record UserSessionMeta(String token, long userId, Instant created, Instant lastUsed) {
    @MappingProvider({"token", "user_id", "created", "last_used"})
    public UserSessionMeta(Row row) throws SQLException {
        this(
                row.getString("token"),
                row.getLong("user_id"),
                row.get("created", INSTANT_TIMESTAMP),
                row.get("last_used", INSTANT_TIMESTAMP));
    }
}
