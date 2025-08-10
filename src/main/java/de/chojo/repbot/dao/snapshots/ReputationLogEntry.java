/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots;

import de.chojo.jdautil.util.MentionUtil;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * A log entry representing a single reputation.
 */
public record ReputationLogEntry(long guildId, long channelId, long donorId, long receiverId, long messageId,
                                 long refMessageId, ThankType type, LocalDateTime received) {
    private static final String PATH = "https://discord.com/channels/%s/%s/%s";
    private static final long DISCORD_EPOCH = 1420070400000L;

    public static ReputationLogEntry build(Row rs) throws SQLException {
        return new ReputationLogEntry(
                rs.getLong("guild_id"),
                rs.getLong("channel_id"),
                rs.getLong("donor_id"),
                rs.getLong("receiver_id"),
                rs.getLong("message_id"),
                rs.getLong("ref_message_id"),
                ThankType.valueOf(rs.getString("cause")),
                rs.getTimestamp("received").toLocalDateTime());
    }

    public String getMessageJumpLink() {
        return String.format(PATH, guildId(), channelId(), messageId());
    }

    public String getRefMessageJumpLink() {
        return String.format(PATH, guildId(), channelId(), refMessageId());
    }

    public boolean hasRefMessage() {
        return refMessageId != 0;
    }

    public String timestamp() {
        var timestamp = ((messageId() >> 22) + DISCORD_EPOCH) / 1000;
        return String.format("<t:%s:d> <t:%s:t>", timestamp, timestamp);
    }

    public String simpleString() {
        return "%s ➜ %s".formatted(MentionUtil.user(receiverId()), MentionUtil.user(receiverId()));
    }

    public Duration tillNow(){
        return Duration.between(received(), Instant.now());
    }

    /**
     * Removes the log entry
     */
    public void delete() {
        query("DELETE FROM reputation_log WHERE message_id = ? AND receiver_id = ? AND donor_id = ?;")
                .single(call().bind(messageId).bind(receiverId).bind(donorId))
                .update();
    }

    /**
     * Removes all reputations associated with the message
     */
    public void deleteAll() {
        query("DELETE FROM reputation_log WHERE message_id = ?")
                .single(call().bind(messageId))
                .update();
    }
}
