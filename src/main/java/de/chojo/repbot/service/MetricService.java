package de.chojo.repbot.service;

import de.chojo.repbot.dao.provider.Metrics;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricService implements Runnable {
    private final Metrics metrics;

    public MetricService(Metrics metrics) {
        this.metrics = metrics;
    }

    public MetricService create(ScheduledExecutorService executorService, Metrics metrics) {
        var now = ZonedDateTime.now();
        var midnight = now.toLocalDate().atStartOfDay().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);
        var minutes = now.until(midnight, ChronoUnit.MINUTES);
        var service = new MetricService(metrics);
        executorService.scheduleAtFixedRate(service, minutes, 1440, TimeUnit.MINUTES);
        return service;
    }

    @Override
    public void run() {
        metrics.reputation().freezeRepCounts();
        if (ZonedDateTime.now().getDayOfMonth() == 1) {
            metrics.users().freezeUserCountMonth();
        }
        if (ZonedDateTime.now().getDayOfWeek() == DayOfWeek.MONDAY) {
            metrics.users().freezeUserCountWeek();
        }
    }
}
