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
    TOTAL("reputationMode.total",
            () -> Instant.EPOCH,
            false),
    ROLLING_WEEK("reputationMode.rollingWeek",
            () -> LocalDate.now()
                           .minusDays(7)
                           .atStartOfDay(ZoneId.of("UTC"))
                           .toInstant(),
            true),
    ROLLING_MONTH("reputationMode.rollingMonth",
            () -> LocalDate.now().minusDays(30)
                           .atStartOfDay(ZoneId.of("UTC"))
                           .toInstant(),
            true),
    WEEK("reputationMode.week",
            () -> LocalDate.now()
                           .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                           .atStartOfDay(ZoneId.of("UTC"))
                           .toInstant(),
            true),
    MONTH("reputationMode.month",
            () -> LocalDate.now()
                           .withDayOfMonth(1)
                           .atStartOfDay(ZoneId.of("UTC"))
                           .toInstant(),
            true);

    private final String localeCode;
    private final Supplier<Instant> dateInit;
    private final boolean autoRefresh;

    ReputationMode(String localeCode, Supplier<Instant> dateInit, boolean autoRefresh) {
        this.localeCode = localeCode;
        this.dateInit = dateInit;
        this.autoRefresh = autoRefresh;
    }

    public Instant dateInit() {
        return dateInit.get();
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public String localeCode() {
        return localeCode;
    }
}
