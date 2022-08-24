package de.chojo.repbot.dao.access.guild.settings.sub;

public enum ReputationMode {
    TOTAL("user_reputation", "reputationMode.total", true, false),
    ROLLING_WEEK("user_reputation_week", "reputationMode.rollingWeek", false, true),
    ROLLING_MONTH("user_reputation_month", "reputationMode.rollingMonth", false, true);

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
