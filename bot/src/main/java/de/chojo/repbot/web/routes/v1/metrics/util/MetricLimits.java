/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics.util;

public record MetricLimits(
        int maxHourOffset,
        int maxDayOffset,
        int maxWeekOffset,
        int maxMonthOffset,
        int maxYearOffset,
        int maxHours,
        int maxDays,
        int maxWeeks,
        int maxMonths) {
    public static MetricLimits fromConstants() {
        return new MetricLimits(
                MetricsRoute.MAX_HOUR_OFFSET,
                MetricsRoute.MAX_DAY_OFFSET,
                MetricsRoute.MAX_WEEK_OFFSET,
                MetricsRoute.MAX_MONTH_OFFSET,
                MetricsRoute.MAX_YEAR_OFFSET,
                MetricsRoute.MAX_HOURS,
                MetricsRoute.MAX_DAYS,
                MetricsRoute.MAX_WEEKS,
                MetricsRoute.MAX_MONTH);
    }
}
