/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.statistic;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.statistic.element.DataStatistic;
import de.chojo.repbot.statistic.element.ProcessStatistics;
import de.chojo.repbot.statistic.element.ShardStatistic;
import de.chojo.repbot.statistic.element.SystemStatistics;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Class responsible for gathering and refreshing statistics for the bot.
 */
public class Statistic {
    private static final Logger log = getLogger(Statistic.class);
    private final ShardManager shardManager;
    private final Metrics metrics;

    /**
     * Constructs a new Statistic instance.
     *
     * @param shardManager the shard manager
     * @param metrics the metrics provider
     */
    private Statistic(ShardManager shardManager, Metrics metrics) {
        this.shardManager = shardManager;
        this.metrics = metrics;
        getSystemStatistic();
    }

    /**
     * Creates a new Statistic instance and schedules periodic refresh of statistics.
     *
     * @param shardManager the shard manager
     * @param metrics the metrics provider
     * @param service the scheduled executor service
     * @return the created Statistic instance
     */
    public static Statistic of(ShardManager shardManager, Metrics metrics, ScheduledExecutorService service) {
        var statistic = new Statistic(shardManager, metrics);
        service.scheduleAtFixedRate(statistic::refreshStatistics, 1, 30, TimeUnit.MINUTES);
        return statistic;
    }

    /**
     * Gathers statistics for a specific shard.
     *
     * @param jda the JDA instance
     * @return the gathered ShardStatistic
     */
    private ShardStatistic getShardStatistic(JDA jda) {
        var shardId = jda.getShardInfo().getShardId();
        var analyzedMessages = metrics.messages().hour(1, 1).get(0).count();

        return new ShardStatistic(
                shardId + 1,
                jda.getStatus(),
                analyzedMessages,
                jda.getGuildCache().size());
    }

    /**
     * Gathers system-wide statistics.
     *
     * @return the gathered SystemStatistics
     */
    public SystemStatistics getSystemStatistic() {
        var shardStatistics = shardManager.getShardCache()
                                          .stream()
                                          .map(this::getShardStatistic)
                                          .collect(Collectors.toList());

        return new SystemStatistics(ProcessStatistics.create(),
                metrics.statistic().getStatistic().orElseGet(DataStatistic::new),
                shardStatistics);
    }

    /**
     * Refreshes the statistics in the metrics provider.
     */
    private void refreshStatistics() {
        metrics.statistic().refreshStatistics();
    }
}
