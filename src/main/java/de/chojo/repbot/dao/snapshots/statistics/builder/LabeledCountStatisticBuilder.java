/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.statistics.builder;

import de.chojo.repbot.dao.snapshots.statistics.CountStatistics;
import de.chojo.repbot.dao.snapshots.statistics.LabeledCountStatistic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class for creating LabeledCountStatistic instances.
 */
public class LabeledCountStatisticBuilder {
    private final Map<String, List<CountStatistics>> stats = new LinkedHashMap<>();

    /**
     * Creates a new LabeledCountStatisticBuilder instance.
     */
    public LabeledCountStatisticBuilder(){
    }

    /**
     * Adds a CountStatistics instance to the builder with the specified label.
     *
     * @param label the label for the statistics
     * @param statistic the CountStatistics instance to add
     * @return the current instance of LabeledCountStatisticBuilder
     */
    public LabeledCountStatisticBuilder add(String label, CountStatistics statistic) {
        stats.computeIfAbsent(label, key -> new ArrayList<>()).add(statistic);
        return this;
    }

    /**
     * Builds and returns a LabeledCountStatistic instance with the accumulated statistics.
     *
     * @return a new LabeledCountStatistic instance
     */
    public LabeledCountStatistic build() {
        return new LabeledCountStatistic(stats);
    }
}
