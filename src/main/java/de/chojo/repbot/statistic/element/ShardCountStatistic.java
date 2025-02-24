/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.statistic.element;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.statistic.EmbedDisplay;
import de.chojo.repbot.statistic.ReplacementProvider;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Represents the statistics of shard counts.
 *
 * @param shardStatistics the list of shard statistics
 */
public record ShardCountStatistic(List<ShardStatistic> shardStatistics) implements ReplacementProvider, EmbedDisplay {

    /**
     * Returns the count of shards.
     *
     * @return the number of shards
     */
    public int shardCount() {
        return shardStatistics.size();
    }

    /**
     * Provides a list of replacements for localization.
     *
     * @return a list of replacements
     */
    @Override
    public List<Replacement> replacements() {
        return Collections.singletonList(Replacement.create("shard_count", shardCount()));
    }

    /**
     * Appends shard statistics to the given embed builder.
     *
     * @param embedBuilder the embed builder to append to
     */
    @Override
    public void appendTo(EmbedBuilder embedBuilder) {
        for (var shard : shardStatistics) {
            embedBuilder.addField("#" + shard.shard(),
                    "Status: " + shard.status().name() + "\nGuilds:" + shard.guilds(), true);
        }
    }
}
