/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

/**
 * Enum representing different reputation modes.
 */
public enum ReputationMode {
    /**
     * Total reputation mode.
     * <p>
     * This mode uses the "user_reputation" table and does not support auto-refresh.
     */
    TOTAL("user_reputation", "reputationMode.total", true, false),

    /**
     * Rolling week reputation mode.
     * <p>
     * This mode uses the "user_reputation_7_days" table and supports auto-refresh.
     */
    ROLLING_WEEK("user_reputation_7_days", "reputationMode.rollingWeek", true, true),

    /**
     * Rolling month reputation mode.
     * <p>
     * This mode uses the "user_reputation_30_days" table and supports auto-refresh.
     */
    ROLLING_MONTH("user_reputation_30_days", "reputationMode.rollingMonth", true, true),

    /**
     * Weekly reputation mode.
     * <p>
     * This mode uses the "user_reputation_week" table and supports auto-refresh.
     */
    WEEK("user_reputation_week", "reputationMode.week", true, true),

    /**
     * Monthly reputation mode.
     * <p>
     * This mode uses the "user_reputation_month" table and supports auto-refresh.
     */
    MONTH("user_reputation_month", "reputationMode.month", true, true);

    private final String tableName;
    private final String localeCode;
    private final boolean supportsOffset;
    private final boolean autoRefresh;

    /**
     * Constructs a ReputationMode enum with the specified parameters.
     *
     * @param tableName the name of the table associated with the reputation mode
     * @param localeCode the locale code for the reputation mode
     * @param supportsOffset whether the reputation mode supports offset
     * @param autoRefresh whether the reputation mode supports auto-refresh
     */
    ReputationMode(String tableName, String localeCode, boolean supportsOffset, boolean autoRefresh) {
        this.tableName = tableName;
        this.localeCode = localeCode;
        this.supportsOffset = supportsOffset;
        this.autoRefresh = autoRefresh;
    }

    /**
     * Returns the table name associated with the reputation mode.
     *
     * @return the table name
     */
    public String tableName() {
        return tableName;
    }

    /**
     * Returns whether the reputation mode supports offset.
     *
     * @return true if the reputation mode supports offset, false otherwise
     */
    public boolean isSupportsOffset() {
        return supportsOffset;
    }

    /**
     * Returns whether the reputation mode supports auto-refresh.
     *
     * @return true if the reputation mode supports auto-refresh, false otherwise
     */
    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    /**
     * Returns the locale code associated with the reputation mode.
     *
     * @return the locale code
     */
    public String localeCode() {
        return localeCode;
    }
}
