/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.function.Supplier;

public enum ReputationMode {
    TOTAL("user_reputation",
            "reputationMode.total",
            () -> Instant.EPOCH,
            true,
            false),
    ROLLING_WEEK("user_reputation_7_days",
            "reputationMode.rollingWeek",
            () -> LocalDate.now()
                           .minusDays(7)
                           .atStartOfDay(ZoneId.of("UTC"))
                           .toInstant()
            , true,
            true),
    ROLLING_MONTH("user_reputation_30_days",
            "reputationMode.rollingMonth",
            () -> LocalDate.now().minusDays(30)
                           .atStartOfDay(ZoneId.of("UTC"))
                           .toInstant(),
            true,
            true),
    WEEK("user_reputation_week",
            "reputationMode.week",
            () -> LocalDate.now()
                           .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                           .atStartOfDay(ZoneId.of("UTC"))
                           .toInstant(),
            true,
            true),
    MONTH("user_reputation_month",
            "reputationMode.month",
            () -> LocalDate.now()
                           .withDayOfMonth(1)
                           .atStartOfDay(ZoneId.of("UTC"))
                           .toInstant(),
            true,
            true);

    private final String tableName;
    private final String localeCode;
    private final Supplier<Instant> dateInit;
    private final boolean supportsOffset;
    private final boolean autoRefresh;

    ReputationMode(String guildRanking, String localeCode, Supplier<Instant> dateInit, boolean supportsOffset, boolean autoRefresh) {
        this.tableName = guildRanking;
        this.localeCode = localeCode;
        this.dateInit = dateInit;
        this.supportsOffset = supportsOffset;
        this.autoRefresh = autoRefresh;
    }

    public Instant dateInit() {
        return dateInit.get();
    }

    public String guildRanking() {
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
