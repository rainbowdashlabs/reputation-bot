package de.chojo.repbot.statistic.element;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.statistic.ReplacementProvider;

import java.util.Collections;
import java.util.List;

public class GlobalShardStatistic implements ReplacementProvider {
    private final long analyzedMessages;

    public GlobalShardStatistic(List<ShardStatistic> shardStatistics) {
        analyzedMessages = shardStatistics.stream()
                .map(ShardStatistic::analyzedMessages).reduce(0L, Long::sum);
    }

    public long analyzedMessages() {
        return analyzedMessages;
    }


    @Override
    public List<Replacement> replacements() {
        return Collections.singletonList(Replacement.create("analyzed_messages", analyzedMessages));
    }
}
