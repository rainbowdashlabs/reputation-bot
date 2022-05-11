package de.chojo.repbot.dao.snapshots;

public record GuildReputationStats(int totalReputation, int weekReputation, int todayReputation, long topChannelId) {
}
