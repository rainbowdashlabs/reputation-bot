package de.chojo.repbot.data.wrapper;

import de.chojo.repbot.analyzer.ThankType;

import java.time.LocalDateTime;

public class ReputationLogEntry {
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

    public ReputationLogEntry(long guildId, long channelId, long donorId, long receiverId, long messageId, long refMessageId, ThankType type, LocalDateTime received) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.donorId = donorId;
        this.receiverId = receiverId;
        this.messageId = messageId;
        this.refMessageId = refMessageId;
        this.type = type;
        this.received = received;
    }

    public String getMessageJumpLink() {
        return String.format(PATH, guildId, channelId, messageId);
    }

    public String getRedMessageJumpLink() {
        return String.format(PATH, guildId, channelId, refMessageId);
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

    public String timestamp(){
        var timestamp = ((messageId() >> 22) + DISCORD_EPOCH) / 1000;
        return String.format("<t:%s:d> <t:%s:t>", timestamp, timestamp);
    }
}
