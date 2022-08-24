package de.chojo.repbot.core;

import de.chojo.repbot.util.LogNotify;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static org.slf4j.LoggerFactory.getLogger;

public class Threading {
    private static final Logger log = getLogger(Threading.class);
    private static final Thread.UncaughtExceptionHandler EXCEPTION_HANDLER =
            (t, e) -> log.error(LogNotify.NOTIFY_ADMIN, "An uncaught exception occured in " + t.getName() + "-" + t.getId() + ".", e);
    private final ThreadGroup eventGroup = new ThreadGroup("Event Worker");
    private final ThreadGroup workerGroup = new ThreadGroup("Scheduled Worker");
    private final ThreadGroup hikariGroup = new ThreadGroup("Hikari Worker");
    private final ThreadGroup jdaGroup = new ThreadGroup("JDA Worker");
    private final ExecutorService eventThreads = Executors.newFixedThreadPool(20, createThreadFactory(eventGroup));
    private final ScheduledExecutorService repBotWorker = Executors.newScheduledThreadPool(3, createThreadFactory(workerGroup));

    public static ThreadFactory createThreadFactory(ThreadGroup group) {
        return r -> {
            var thread = new Thread(group, r, group.getName());
            thread.setUncaughtExceptionHandler(EXCEPTION_HANDLER);
            return thread;
        };
    }

    public ThreadGroup eventGroup() {
        return eventGroup;
    }

    public ThreadGroup workerGroup() {
        return workerGroup;
    }

    public ThreadGroup hikariGroup() {
        return hikariGroup;
    }

    public ThreadGroup jdaGroup() {
        return jdaGroup;
    }

    public ExecutorService eventThreads() {
        return eventThreads;
    }

    public ScheduledExecutorService repBotWorker() {
        return repBotWorker;
    }

    public void shutdown() {
        repBotWorker.shutdown();
    }
}
