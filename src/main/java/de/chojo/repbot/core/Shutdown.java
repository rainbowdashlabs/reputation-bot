/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.core;

import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Handles the shutdown process for the bot, including shutting down the shard manager,
 * scheduler, and database connections.
 */
public class Shutdown {
    private static final Logger log = getLogger(Shutdown.class);
    private final Bot bot;
    private final Threading threading;
    private final Data data;

    /**
     * Constructs a new Shutdown instance.
     *
     * @param bot the bot instance
     * @param threading the threading instance
     * @param data the data instance
     */
    private Shutdown(Bot bot, Threading threading, Data data) {
        this.bot = bot;
        this.threading = threading;
        this.data = data;
    }

    /**
     * Creates and initializes a new Shutdown instance.
     *
     * @param bot the bot instance
     * @param threading the threading instance
     * @param data the data instance
     * @return the created Shutdown instance
     */
    public static Shutdown create(Bot bot, Threading threading, Data data) {
        var shutdown = new Shutdown(bot, threading, data);
        shutdown.init();
        return shutdown;
    }

    /**
     * Initializes the shutdown hook to handle the shutdown process.
     */
    public void init() {
        log.info("Creating Shutdown Hook");
        var shutdown = new Thread(() -> {
            log.info("Shutting down shard manager.");
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
