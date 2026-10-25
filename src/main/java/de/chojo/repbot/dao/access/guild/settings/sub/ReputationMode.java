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
import java.util.function.Function;

public enum ReputationMode {
    TOTAL("reputationMode.total", day -> Instant.EPOCH, false),
    ROLLING_WEEK("reputationMode.rollingWeek", day -> startOfDay(day.minusDays(7)), true),
    ROLLING_MONTH("reputationMode.rollingMonth", day -> startOfDay(day.minusDays(30)), true),
    WEEK("reputationMode.week", day -> startOfDay(day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))), true),
    MONTH("reputationMode.month", day -> startOfDay(day.withDayOfMonth(1)), true);

    private final String localeCode;
    private final Function<LocalDate, Instant> dateInit;
    private final boolean autoRefresh;

    ReputationMode(String localeCode, Function<LocalDate, Instant> dateInit, boolean autoRefresh) {
        this.localeCode = localeCode;
        this.dateInit = dateInit;
        this.autoRefresh = autoRefresh;
    }

    private static Instant startOfDay(LocalDate day) {
        return day.atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    /**
     * Get the point in time from which reputation counts today.
     *
     * @return start of the reputation window
     */
    public Instant dateInit() {
        return dateInit(LocalDate.now());
    }

    /**
     * Get the point in time from which reputation counted on the given day.
     *
     * @param day the day to evaluate the window for
     * @return start of the reputation window on that day
     */
    public Instant dateInit(LocalDate day) {
        return dateInit.apply(day);
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public String localeCode() {
        return localeCode;
    }
}
