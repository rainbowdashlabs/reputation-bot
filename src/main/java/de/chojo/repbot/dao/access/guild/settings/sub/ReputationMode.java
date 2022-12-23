package de.chojo.repbot.dao.access.guild.settings.sub;

public enum ReputationMode {
    TOTAL("user_reputation", "reputationMode.total", true, false),
    ROLLING_WEEK("user_reputation_7_days", "reputationMode.rollingWeek", true, true),
    ROLLING_MONTH("user_reputation_30_days", "reputationMode.rollingMonth", true, true),
    WEEK("user_reputation_week", "reputationMode.week", true, true),
    MONTH("user_reputation_month", "reputationMode.month", true, true);

    private final String tableName;
    private final String localeCode;
    private final boolean supportsOffset;
    private final boolean autoRefresh;

    ReputationMode(String tableName, String localeCode, boolean supportsOffset, boolean autoRefresh) {
        this.tableName = tableName;
        this.localeCode = localeCode;
        this.supportsOffset = supportsOffset;
        this.autoRefresh = autoRefresh;
    }

    public String tableName() {
        return tableName;
    }

    public boolean isSupportsOffset() {
        return supportsOffset;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public String localeCode() {
        return localeCode;
    }
}
