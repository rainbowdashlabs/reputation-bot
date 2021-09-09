package de.chojo.repbot.data.wrapper;

public class GuildReputationStats {
    private final int totalReputation;
    private final int weekReputation;
    private final int todayReputation;
    private final long topChannelId;

    public GuildReputationStats(int totalReputation, int weekReputation, int todayReputation, long topChannelId) {
        this.totalReputation = totalReputation;
        this.weekReputation = weekReputation;
        this.todayReputation = todayReputation;
        this.topChannelId = topChannelId;
    }

    public int totalReputation() {
        return totalReputation;
    }

    public int weekReputation() {
        return weekReputation;
    }

    public int todayReputation() {
        return todayReputation;
    }

    public long topChannelId() {
        return topChannelId;
    }
}
