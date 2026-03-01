/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.vote;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;
import java.time.Instant;

import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public record VoteLog(
        @JsonSerialize(using = ToStringSerializer.class) long userId,
        @JsonSerialize(using = ToStringSerializer.class) long guildId,
        int tokens,
        VoteReason reason,
        Instant created) {

    public static VoteLog map(Row row) throws SQLException {
        return new VoteLog(
                row.getLong("user_id"),
                row.getLong("guild_id"),
                row.getInt("tokens"),
                row.getEnum("reason", VoteReason.class),
                row.get("created", INSTANT_TIMESTAMP));
    }
}
