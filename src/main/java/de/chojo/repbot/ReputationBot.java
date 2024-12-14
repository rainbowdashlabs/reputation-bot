/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.core.Bot;
import de.chojo.repbot.core.Data;
import de.chojo.repbot.core.Localization;
import de.chojo.repbot.core.Shutdown;
import de.chojo.repbot.core.Threading;
import de.chojo.repbot.core.Web;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Main class for the ReputationBot application.
 */
public class ReputationBot {
    private static ReputationBot instance;

    /**
     * Creates a new ReputationBot instance.
     */
    private ReputationBot() {
    }

    /**
     * Main method to start the ReputationBot application.
     *
     * @param args the command line arguments
     * @throws SQLException If the database connection fails.
     * @throws IOException  If the configuration file fails to load.
     * @throws LoginException If the bot login fails.
     */
    public static void main(String[] args) throws SQLException, IOException, LoginException {
        ReputationBot.instance = new ReputationBot();
        instance.start();
    }

    /**
     * Starts the bot.
     *
     * @throws SQLException If the database connection fails.
     * @throws IOException  If the configuration file fails to load.
     * @throws LoginException If the bot login fails.
     */
    private void start() throws SQLException, IOException, LoginException {
        var configuration = Configuration.create();

        var threading = new Threading();

        var data = Data.create(threading, configuration);

        var localization = Localization.create(data);

        var bot = Bot.create(data, threading, configuration, localization);

        Shutdown.create(bot, threading, data);

        Web.create(bot, data, threading, configuration);
    }
}
