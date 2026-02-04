/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.dao.provider.Metrics;

import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricService implements Runnable {
    private final Metrics metrics;

    public MetricService(Metrics metrics) {
        this.metrics = metrics;
    }

    public static MetricService create(ScheduledExecutorService executorService, Metrics metrics) {
        var now = ZonedDateTime.now(ZoneOffset.UTC);
        var base = now.toLocalDate().atStartOfDay().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)
                      .atOffset(ZoneOffset.UTC);
        var minutes = now.until(base, ChronoUnit.MINUTES);
        var service = new MetricService(metrics);
        executorService.scheduleAtFixedRate(service, minutes, 1440, TimeUnit.MINUTES);
        return service;
    }

    @Override
    public void run() {
        metrics.reputation().saveRepCounts();
        if (ZonedDateTime.now().getDayOfMonth() == 1) {
            metrics.users().saveUserCountMonth();
        }
        if (ZonedDateTime.now().getDayOfWeek() == DayOfWeek.MONDAY) {
            metrics.users().saveUserCountWeek();
        }
    }
}
