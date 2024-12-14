/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.statistic.Statistic;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for managing the bot's presence.
 */
public class PresenceService implements Runnable {
    private static final Logger log = getLogger(PresenceService.class);
    private final ShardManager shardManager;
    private final Configuration configuration;
    private final Statistic statistic;

    /**
     * Constructs a PresenceService with the specified shard manager, configuration, and statistic.
     *
     * @param shardManager the shard manager
     * @param configuration the configuration
     * @param statistic the statistic
     */
    public PresenceService(ShardManager shardManager, Configuration configuration, Statistic statistic) {
        this.shardManager = shardManager;
        this.configuration = configuration;
        this.statistic = statistic;
    }

    /**
     * Starts the presence service if it is active in the configuration.
     *
     * @param shardManager the shard manager
     * @param configuration the configuration
     * @param statistic the statistic
     * @param executorService the scheduled executor service
     */
    public static void start(ShardManager shardManager, Configuration configuration, Statistic statistic, ScheduledExecutorService executorService) {
        var presenceService = new PresenceService(shardManager, configuration, statistic);
        if (configuration.presence().isActive()) {
            executorService.scheduleAtFixedRate(presenceService, 0,
                    configuration.presence().interval(), TimeUnit.MINUTES);
        }
    }

    /**
     * Runs the presence service, refreshing the presence if it is active.
     */
    @Override
    public void run() {
        if (!configuration.presence().isActive()) return;
        refresh();
    }

    /**
     * Refreshes the bot's presence based on the current configuration and statistics.
     */
    private void refresh() {
        var replacements = statistic.getSystemStatistic().replacements();
        var currentPresence = configuration.presence().randomStatus();
        var text = currentPresence.text(replacements);
        log.debug("Changed presence to: {}", text);

        shardManager.setPresence(OnlineStatus.ONLINE,
                Activity.of(currentPresence.type(), text));
    }
}
