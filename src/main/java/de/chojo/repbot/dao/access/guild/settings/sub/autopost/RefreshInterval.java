/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.autopost;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.function.Predicate;

public enum RefreshInterval {
    /**
     * Send on every full hour.
     */
    HOURLY(now -> true),
    /**
     * Send at midnight UTC.
     */
    DAILY(now -> now.getHour() == 0),
    /**
     * Send at midnight UTC every Monday.
     */
    WEEKLY(now -> now.get(ChronoField.DAY_OF_WEEK) == 1 && now.getHour() == 0),
    /**
     * Send at midnight UTC every first day of the month.
     */
    MONTHLY(now -> now.getDayOfMonth() == 1 && now.getHour() == 0);

    private final Predicate<LocalDateTime> applicable;

    RefreshInterval(Predicate<LocalDateTime> applicable) {
        this.applicable = applicable;
    }

    public boolean isApplicable(LocalDateTime now) {
        return applicable.test(now);
    }

    @Override
    public String toString() {
        return "%s.%s.name".formatted(getClass().getSimpleName().toLowerCase(), name().toLowerCase());
    }
}
