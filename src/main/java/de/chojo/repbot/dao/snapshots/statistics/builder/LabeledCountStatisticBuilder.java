package de.chojo.repbot.dao.snapshots.statistics.builder;

import de.chojo.repbot.dao.snapshots.statistics.CountStatistics;
import de.chojo.repbot.dao.snapshots.statistics.LabeledCountStatistic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LabeledCountStatisticBuilder {
    private final Map<String, List<CountStatistics>> stats = new LinkedHashMap<>();
    public LabeledCountStatisticBuilder add(String label, CountStatistics statistic) {
        stats.computeIfAbsent(label, key -> new ArrayList<>()).add(statistic);
        return this;
    }

    public LabeledCountStatistic build() {
        return new LabeledCountStatistic(stats);
    }
}
