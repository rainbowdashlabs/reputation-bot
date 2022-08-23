package de.chojo.repbot.dao.snapshots;

import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.dao.access.guild.reputation.sub.Log;
import de.chojo.sqlutil.base.QueryFactoryHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * A log entry representing a single reputation.
 */
public class ReputationLogEntry extends QueryFactoryHolder {
    private static final String PATH = "https://discord.com/channels/%s/%s/%s";
    private static final long DISCORD_EPOCH = 1420070400000L;
    private final long guildId;
    private final long channelId;
    private final long donorId;
    private final long receiverId;
    private final long messageId;
    private final long refMessageId;
    private final ThankType type;
    private final LocalDateTime received;

    public ReputationLogEntry(Log log, long guildId, long channelId, long donorId, long receiverId, long messageId, long refMessageId, ThankType type, LocalDateTime received) {
        super(log);
        this.guildId = guildId;
        this.channelId = channelId;
        this.donorId = donorId;
        this.receiverId = receiverId;
        this.messageId = messageId;
        this.refMessageId = refMessageId;
        this.type = type;
        this.received = received;
    }

    public static ReputationLogEntry build(Log log, ResultSet rs) throws SQLException {
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

    public String getMessageJumpLink() {
        return String.format(PATH, guildId(), channelId(), messageId());
    }

    public String getRefMessageJumpLink() {
        return String.format(PATH, guildId(), channelId(), refMessageId());
    }

    public boolean hasRefMessage() {
        return refMessageId != 0;
    }

    public long guildId() {
        return guildId;
    }

    public long channelId() {
        return channelId;
    }

    public long donorId() {
        return donorId;
    }

    public long receiverId() {
        return receiverId;
    }

    public long messageId() {
        return messageId;
    }

    public long refMessageId() {
        return refMessageId;
    }

    public ThankType type() {
        return type;
    }

    public LocalDateTime received() {
        return received;
    }

    public String timestamp() {
        var timestamp = ((messageId() >> 22) + DISCORD_EPOCH) / 1000;
        return String.format("<t:%s:d> <t:%s:t>", timestamp, timestamp);
    }

    /**
     * Removes the log entry
     */
    public void delete() {
        builder()
                .query("DELETE FROM reputation_log WHERE message_id = ? AND receiver_id = ? AND donor_id = ?;")
                .paramsBuilder(stmt -> stmt.setLong(messageId).setLong(receiverId).setLong(donorId))
                .update().execute();
    }

    /**
     * Removes all reputations associated with the message
     */
    public void deleteAll() {
        builder()
                .query("DELETE FROM reputation_log WHERE message_id = ?")
                .paramsBuilder(stmt -> stmt.setLong(messageId))
                .update().execute();
    }
}
