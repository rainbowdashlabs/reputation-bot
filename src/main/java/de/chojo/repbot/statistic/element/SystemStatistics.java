/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.statistic.element;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.statistic.EmbedDisplay;
import de.chojo.repbot.statistic.ReplacementProvider;
import de.chojo.repbot.statistic.display.GlobalInfoStatisticDisplay;
import de.chojo.repbot.statistic.display.SystemInfoStatisticDisplay;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing system statistics, including process statistics, data statistics, and shard statistics.
 */
public class SystemStatistics implements ReplacementProvider, EmbedDisplay {

    private final ProcessStatistics processStatistics;
    private final DataStatistic dataStatistic;
    private final GlobalShardStatistic aggregatedShards;
    private final ShardCountStatistic shardCountStatistic;
    private final GlobalInfoStatisticDisplay globalInfoStatisticDisplay;
    private final SystemInfoStatisticDisplay systemInfoStatisticDisplay;

    /**
     * Constructs a new SystemStatistics instance.
     *
     * @param processStatistics the process statistics
     * @param dataStatistic the data statistics
     * @param shardStatistics the list of shard statistics
     */
    public SystemStatistics(ProcessStatistics processStatistics, DataStatistic dataStatistic,
                            List<ShardStatistic> shardStatistics) {
        this.processStatistics = processStatistics;
        this.dataStatistic = dataStatistic;
        shardCountStatistic = new ShardCountStatistic(shardStatistics);
        aggregatedShards = new GlobalShardStatistic(shardStatistics);
        globalInfoStatisticDisplay = new GlobalInfoStatisticDisplay(aggregatedShards,
                dataStatistic);
        systemInfoStatisticDisplay = new SystemInfoStatisticDisplay(shardCountStatistic,
                dataStatistic);

    }

    /**
     * Retrieves the process statistics.
     *
     * @return the process statistics
     */
    public ProcessStatistics processStatistics() {
        return processStatistics;
    }

    /**
     * Appends the system statistics to the given EmbedBuilder.
     *
     * @param embedBuilder the EmbedBuilder to append to
     */
    @Override
    public void appendTo(EmbedBuilder embedBuilder) {
        systemInfoStatisticDisplay.appendTo(embedBuilder);
        processStatistics.appendTo(embedBuilder);
        globalInfoStatisticDisplay.appendTo(embedBuilder);
        shardCountStatistic.appendTo(embedBuilder);
    }

    /**
     * Retrieves the aggregated shard statistics.
     *
     * @return the aggregated shard statistics
     */
    public GlobalShardStatistic aggregatedShards() {
        return aggregatedShards;
    }

    /**
     * Retrieves the data statistics.
     *
     * @return the data statistics
     */
    public DataStatistic dataStatistic() {
        return dataStatistic;
    }

    /**
     * Retrieves the shard count.
     *
     * @return the shard count
     */
    public int shardCount() {
        return shardCountStatistic.shardCount();
    }

    /**
     * Retrieves the list of replacements for the system statistics.
     *
     * @return the list of replacements
     */
    @Override
    public List<Replacement> replacements() {
        List<Replacement> replacements = new ArrayList<>();
        replacements.add(Replacement.create("shard_count", shardCount()));
        replacements.addAll(aggregatedShards().replacements());
        replacements.addAll(dataStatistic().replacements());
        replacements.addAll(processStatistics().replacements());
        return replacements;
    }
}
