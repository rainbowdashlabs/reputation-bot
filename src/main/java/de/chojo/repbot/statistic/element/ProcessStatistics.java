package de.chojo.repbot.statistic.element;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.statistic.ReplacementProvider;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ProcessStatistics implements ReplacementProvider {
    private static final int MB = 1024 * 1024;
    private static final Instant START = Instant.now();
    private final long total;
    private final long used;
    private final long free;
    private final long max;
    private final long threads;

    public ProcessStatistics(long total, long used, long free, long max, long threads) {
        this.total = total;
        this.used = used;
        this.free = free;
        this.max = max;
        this.threads = threads;
    }

    public long total() {
        return total;
    }

    public long used() {
        return used;
    }

    public long free() {
        return free;
    }

    public long max() {
        return max;
    }

    public long threads() {
        return threads;
    }

    public static ProcessStatistics create() {
        var instance = Runtime.getRuntime();
        var total = instance.totalMemory() / MB;
        var free = instance.freeMemory() / MB;
        var used = total - free / MB;
        var max = instance.maxMemory() / MB;

        return new ProcessStatistics(total, free, used, max, Thread.activeCount());
    }

    public long uptime(){
        return START.until(Instant.now(), ChronoUnit.MILLIS);
    }

    @Override
    public Replacement[] replacements() {
        return new Replacement[]{Replacement.create("total_mem", total), Replacement.create("used_mem", used),
                Replacement.create("free_mem", free), Replacement.create("max_mem", max),
                Replacement.create("threads", threads)};
    }
}
