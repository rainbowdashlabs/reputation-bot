/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.statistic.element;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.statistic.ReplacementProvider;

import java.util.Collections;
import java.util.List;

/**
 * Class representing global shard statistics.
 */
public class GlobalShardStatistic implements ReplacementProvider {
    private final long analyzedMessages;

    /**
     * Constructs a new GlobalShardStatistic instance.
     *
     * @param shardStatistics a list of ShardStatistic instances
     */
    public GlobalShardStatistic(List<ShardStatistic> shardStatistics) {
        analyzedMessages = shardStatistics.stream()
                                          .map(ShardStatistic::analyzedMessages).reduce(0L, Long::sum);
    }

    /**
     * Retrieves the total number of analyzed messages.
     *
     * @return the total number of analyzed messages
     */
    public long analyzedMessages() {
        return analyzedMessages;
    }

    /**
     * Provides a list of replacements for localization.
     *
     * @return a list of Replacement instances
     */
    @Override
    public List<Replacement> replacements() {
        return Collections.singletonList(Replacement.create("analyzed_messages", analyzedMessages));
    }
}
