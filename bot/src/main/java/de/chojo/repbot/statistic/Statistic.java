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

public class Statistic {
    private static final Logger log = getLogger(Statistic.class);
    private final ShardManager shardManager;
    private final Metrics metrics;

    private Statistic(ShardManager shardManager, Metrics metrics) {
        this.shardManager = shardManager;
        this.metrics = metrics;
        getSystemStatistic();
    }

    public static Statistic of(ShardManager shardManager, Metrics metrics, ScheduledExecutorService service) {
        var statistic = new Statistic(shardManager, metrics);
        service.scheduleAtFixedRate(statistic::refreshStatistics, 1, 30, TimeUnit.MINUTES);
        return statistic;
    }

    public SystemStatistics getSystemStatistic() {
        var shardStatistics = shardManager.getShardCache().stream()
                .map(this::getShardStatistic)
                .collect(Collectors.toList());

        return new SystemStatistics(
                ProcessStatistics.create(),
                metrics.statistic().getStatistic().orElseGet(DataStatistic::new),
                shardStatistics);
    }

    private ShardStatistic getShardStatistic(JDA jda) {
        var shardId = jda.getShardInfo().getShardId();
        var analyzedMessages = metrics.messages().hour(1, 1).get(0).count();

        return new ShardStatistic(
                shardId + 1,
                jda.getStatus(),
                analyzedMessages,
                jda.getGuildCache().size());
    }

    private void refreshStatistics() {
        metrics.statistic().refreshStatistics();
    }
}
