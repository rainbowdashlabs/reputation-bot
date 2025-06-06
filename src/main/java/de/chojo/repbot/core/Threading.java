/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.core;

import de.chojo.repbot.util.LogNotify;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import static org.slf4j.LoggerFactory.getLogger;

public class Threading {
    private static final Logger log = getLogger(Threading.class);
    private static final Thread.UncaughtExceptionHandler EXCEPTION_HANDLER =
            (t, e) -> {
                log.error(LogNotify.NOTIFY_ADMIN, "An uncaught exception occurred in {}-{}.", t.getName(), t.threadId(), e);
                e.printStackTrace();
            };
    private final ThreadGroup eventGroup = new ThreadGroup("Event Worker");
    private final ThreadGroup workerGroup = new ThreadGroup("Scheduled Worker");
    private final ThreadGroup hikariGroup = new ThreadGroup("Hikari Worker");
    private final ThreadGroup jdaGroup = new ThreadGroup("JDA Worker");
    private final ExecutorService eventThreads = Executors.newVirtualThreadPerTaskExecutor();
    private final ScheduledExecutorService repBotWorker = new Executor(5, createThreadFactory(workerGroup));

    public static ThreadFactory createThreadFactory(ThreadGroup group) {
        return r -> new Thread(group, () -> {
            try {
                r.run();
            } catch (Throwable e) {
                log.error("An uncaught exception occurred in {}-{}.", Thread.currentThread().getName(), Thread.currentThread().threadId(), e);
            }
        }, group.getName());
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

    private static class Executor extends ScheduledThreadPoolExecutor {

        public Executor(int corePoolSize, @NotNull ThreadFactory threadFactory) {
            super(corePoolSize, threadFactory);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            if (t == null && r instanceof Future<?> future) {
                try {
                    if (future.isDone()) {
                        future.get();
                    }
                } catch (CancellationException e) {
                    return;
                } catch (ExecutionException e) {
                    t = e.getCause();
                }catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (t != null) {
                log.error(LogNotify.NOTIFY_ADMIN, "An uncaught exception occurred.", t);
            }
        }
    }
}
