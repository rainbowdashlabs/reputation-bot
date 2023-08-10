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

public class SystemStatistics implements ReplacementProvider, EmbedDisplay {

    private final ProcessStatistics processStatistics;
    private final DataStatistic dataStatistic;
    private final GlobalShardStatistic aggregatedShards;
    private final ShardCountStatistic shardCountStatistic;
    private final GlobalInfoStatisticDisplay globalInfoStatisticDisplay;
    private final SystemInfoStatisticDisplay systemInfoStatisticDisplay;

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

    public ProcessStatistics processStatistics() {
        return processStatistics;
    }

    @Override
    public void appendTo(EmbedBuilder embedBuilder) {
        systemInfoStatisticDisplay.appendTo(embedBuilder);
        processStatistics.appendTo(embedBuilder);
        globalInfoStatisticDisplay.appendTo(embedBuilder);
        shardCountStatistic.appendTo(embedBuilder);
    }

    public GlobalShardStatistic aggregatedShards() {
        return aggregatedShards;
    }

    public DataStatistic dataStatistic() {
        return dataStatistic;
    }

    public int shardCount() {
        return shardCountStatistic.shardCount();
    }

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
