/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot;


import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public class TestScratch {
    @Test
    public void startOfWeek() {
        Instant weekStart = LocalDate.now()
                                     .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                                     .atStartOfDay(ZoneId.of("UTC"))
                                     .toInstant();
        System.out.println(weekStart);
    }

    @Test
    public void startOfMonth() {
        Instant weekStart = LocalDate.now()
                                     .withDayOfMonth(1)
                                     .atStartOfDay(ZoneId.of("UTC"))
                                     .toInstant();
        System.out.println(weekStart);
    }

    @Test
    public void last7Days() {
        Instant weekStart = LocalDate.now()
                                     .minusDays(7)
                                     .atStartOfDay(ZoneId.of("UTC"))
                                     .toInstant();
        System.out.println(weekStart);
    }

    @Test
    public void lastMonth() {
        Instant weekStart = LocalDate.now().minusDays(30)
                                     .atStartOfDay(ZoneId.of("UTC"))
                                     .toInstant();
        System.out.println(weekStart);
    }
}
