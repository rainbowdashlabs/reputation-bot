/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.core;

import de.chojo.repbot.util.LogNotify;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Class for managing threading and executor services.
 */
public class Threading {
    /**
     * Logger instance for logging events.
     */
    private static final Logger log = getLogger(Threading.class);

    /**
     * Uncaught exception handler for logging uncaught exceptions.
     */
    private static final Thread.UncaughtExceptionHandler EXCEPTION_HANDLER =
            (t, e) -> log.error(LogNotify.NOTIFY_ADMIN, "An uncaught exception occured in " + t.getName() + "-" + t.getId() + ".", e);

    /**
     * Thread group for event workers.
     */
    private final ThreadGroup eventGroup = new ThreadGroup("Event Worker");

    /**
     * Thread group for scheduled workers.
     */
    private final ThreadGroup workerGroup = new ThreadGroup("Scheduled Worker");

    /**
     * Thread group for Hikari workers.
     */
    private final ThreadGroup hikariGroup = new ThreadGroup("Hikari Worker");

    /**
     * Thread group for JDA workers.
     */
    private final ThreadGroup jdaGroup = new ThreadGroup("JDA Worker");

    /**
     * Executor service for event threads.
     */
    private final ExecutorService eventThreads = Executors.newFixedThreadPool(20, createThreadFactory(eventGroup));

    /**
     * Scheduled executor service for RepBot workers.
     */
    private final ScheduledExecutorService repBotWorker = Executors.newScheduledThreadPool(3, createThreadFactory(workerGroup));

    /**
     * Creates a thread factory for the given thread group.
     *
     * @param group the thread group
     * @return the thread factory
     */
    public static ThreadFactory createThreadFactory(ThreadGroup group) {
        return r -> {
            var thread = new Thread(group, r, group.getName());
            thread.setUncaughtExceptionHandler(EXCEPTION_HANDLER);
            return thread;
        };
    }

    /**
     * Creates a new threading instance.
     */
    public Threading(){
    }

    /**
     * Retrieves the event thread group.
     *
     * @return the event thread group
     */
    public ThreadGroup eventGroup() {
        return eventGroup;
    }

    /**
     * Retrieves the worker thread group.
     *
     * @return the worker thread group
     */
    public ThreadGroup workerGroup() {
        return workerGroup;
    }

    /**
     * Retrieves the Hikari thread group.
     *
     * @return the Hikari thread group
     */
    public ThreadGroup hikariGroup() {
        return hikariGroup;
    }

    /**
     * Retrieves the JDA thread group.
     *
     * @return the JDA thread group
     */
    public ThreadGroup jdaGroup() {
        return jdaGroup;
    }

    /**
     * Retrieves the event executor service.
     *
     * @return the event executor service
     */
    public ExecutorService eventThreads() {
        return eventThreads;
    }

    /**
     * Retrieves the scheduled executor service for RepBot workers.
     *
     * @return the scheduled executor service for RepBot workers
     */
    public ScheduledExecutorService repBotWorker() {
        return repBotWorker;
    }

    /**
     * Shuts down the scheduled executor service for RepBot workers.
     */
    public void shutdown() {
        repBotWorker.shutdown();
    }
}
