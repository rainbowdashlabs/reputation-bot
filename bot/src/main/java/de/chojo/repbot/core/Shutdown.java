/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.core;

import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Shutdown {
    private static final Logger log = getLogger(Shutdown.class);
    private final Bot bot;
    private final Threading threading;
    private final Data data;

    private Shutdown(Bot bot, Threading threading, Data data) {
        this.bot = bot;
        this.threading = threading;
        this.data = data;
    }

    public static Shutdown create(Bot bot, Threading threading, Data data) {
        var shutdown = new Shutdown(bot, threading, data);
        shutdown.init();
        return shutdown;
    }

    public void init() {
        log.info("Creating Shutdown Hook");
        var shutdown = new Thread(() -> {
            log.info("Shuting down shardmanager.");
            bot.shutdown();
            log.info("Shutting down scheduler.");
            threading.shutdown();
            log.info("Shutting down database connections.");
            data.shutDown();
            log.info("Bot shutdown complete.");
            LogManager.shutdown();
        });
        Runtime.getRuntime().addShutdownHook(shutdown);
    }
}
