package de.chojo.repbot.statistic.element;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.statistic.ReplacementProvider;

import java.util.Collections;
import java.util.List;

public class SystemStatistics implements ReplacementProvider {
    private final ProcessStatistics processStatistics;
    private final DataStatistic dataStatistic;
    private final GlobalShardStatistic aggregatedShards;
    private final List<ShardStatistic> shardStatistics;

    public SystemStatistics(ProcessStatistics processStatistics, DataStatistic dataStatistic, List<ShardStatistic> shardStatistics) {
        this.processStatistics = processStatistics;
        this.dataStatistic = dataStatistic;
        this.shardStatistics = shardStatistics;
        aggregatedShards = new GlobalShardStatistic(shardStatistics);
    }

    public ProcessStatistics processStatistics() {
        return processStatistics;
    }

    public List<ShardStatistic> shardStatistics() {
        return shardStatistics;
    }

    public GlobalShardStatistic aggregatedShards() {
        return aggregatedShards;
    }

    public DataStatistic dataStatistic() {
        return dataStatistic;
    }

    public int shardCount() {
        return shardStatistics.size();
    }

    @Override
    public List<Replacement> replacements() {
        return Collections.singletonList(Replacement.create("shard_count", shardCount()));
    }
}
