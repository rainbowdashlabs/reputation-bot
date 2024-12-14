/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots;

import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.dao.access.guild.reputation.sub.Log;
import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * A log entry representing a single reputation.
 */
public class ReputationLogEntry  {
    /**
     * URL format for Discord message jump links.
     */
    private static final String PATH = "https://discord.com/channels/%s/%s/%s";
    /**
     * Discord epoch timestamp.
     */
    private static final long DISCORD_EPOCH = 1420070400000L;
    private final long guildId;
    private final long channelId;
    private final long donorId;
    private final long receiverId;
    private final long messageId;
    private final long refMessageId;
    private final ThankType type;
    private final LocalDateTime received;

    /**
     * Constructs a new ReputationLogEntry.
     *
     * @param log the log instance
     * @param guildId the ID of the guild
     * @param channelId the ID of the channel
     * @param donorId the ID of the donor
     * @param receiverId the ID of the receiver
     * @param messageId the ID of the message
     * @param refMessageId the ID of the reference message
     * @param type the type of thank
     * @param received the timestamp when the reputation was received
     */
    public ReputationLogEntry(Log log, long guildId, long channelId, long donorId, long receiverId, long messageId, long refMessageId, ThankType type, LocalDateTime received) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.donorId = donorId;
        this.receiverId = receiverId;
        this.messageId = messageId;
        this.refMessageId = refMessageId;
        this.type = type;
        this.received = received;
    }

    /**
     * Builds a ReputationLogEntry from a database row.
     *
     * @param log the log instance
     * @param rs the database row
     * @return the created ReputationLogEntry
     * @throws SQLException if a database access error occurs
     */
    public static ReputationLogEntry build(Log log, Row rs) throws SQLException {
        return new ReputationLogEntry(log,
                rs.getLong("guild_id"),
                rs.getLong("channel_id"),
                rs.getLong("donor_id"),
                rs.getLong("receiver_id"),
                rs.getLong("message_id"),
                rs.getLong("ref_message_id"),
                ThankType.valueOf(rs.getString("cause")),
                rs.getTimestamp("received").toLocalDateTime());
    }

    /**
     * Retrieves the jump link for the message.
     *
     * @return the message jump link
     */
    public String getMessageJumpLink() {
        return String.format(PATH, guildId(), channelId(), messageId());
    }

    /**
     * Retrieves the jump link for the reference message.
     *
     * @return the reference message jump link
     */
    public String getRefMessageJumpLink() {
        return String.format(PATH, guildId(), channelId(), refMessageId());
    }

    /**
     * Checks if there is a reference message.
     *
     * @return true if there is a reference message, false otherwise
     */
    public boolean hasRefMessage() {
        return refMessageId != 0;
    }

    /**
     * Retrieves the guild ID.
     *
     * @return the guild ID
     */
    public long guildId() {
        return guildId;
    }

    /**
     * Retrieves the channel ID.
     *
     * @return the channel ID
     */
    public long channelId() {
        return channelId;
    }

    /**
     * Retrieves the donor ID.
     *
     * @return the donor ID
     */
    public long donorId() {
        return donorId;
    }

    /**
     * Retrieves the receiver ID.
     *
     * @return the receiver ID
     */
    public long receiverId() {
        return receiverId;
    }

    /**
     * Retrieves the message ID.
     *
     * @return the message ID
     */
    public long messageId() {
        return messageId;
    }

    /**
     * Retrieves the reference message ID.
     *
     * @return the reference message ID
     */
    public long refMessageId() {
        return refMessageId;
    }

    /**
     * Retrieves the type of thank.
     *
     * @return the type of thank
     */
    public ThankType type() {
        return type;
    }

    /**
     * Retrieves the timestamp when the reputation was received.
     *
     * @return the timestamp when the reputation was received
     */
    public LocalDateTime received() {
        return received;
    }

    /**
     * Retrieves the timestamp in Discord format.
     *
     * @return the timestamp in Discord format
     */
    public String timestamp() {
        var timestamp = ((messageId() >> 22) + DISCORD_EPOCH) / 1000;
        return String.format("<t:%s:d> <t:%s:t>", timestamp, timestamp);
    }

    /**
     * Removes the log entry.
     */
    public void delete() {
        query("DELETE FROM reputation_log WHERE message_id = ? AND receiver_id = ? AND donor_id = ?;")
                .single(call().bind(messageId).bind(receiverId).bind(donorId))
                .update();
    }

    /**
     * Removes all reputations associated with the message.
     */
    public void deleteAll() {
        query("DELETE FROM reputation_log WHERE message_id = ?")
                .single(call().bind(messageId))
                .update();
    }
}
