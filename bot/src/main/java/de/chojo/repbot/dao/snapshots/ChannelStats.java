/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots;

import de.chojo.jdautil.util.MentionUtil;
import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;

public record ChannelStats(long channelId, long count) {
    public static ChannelStats build(Row row) throws SQLException {
        return new ChannelStats(row.getLong("channel_id"), row.getLong("count"));
    }

    public String fancyString() {
        return "%s ➜ %d".formatted(MentionUtil.channel(channelId()), count());
    }
}
