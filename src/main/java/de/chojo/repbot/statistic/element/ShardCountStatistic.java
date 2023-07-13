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

public record ShardCountStatistic(List<ShardStatistic> shardStatistics) implements ReplacementProvider, EmbedDisplay {

    public int shardCount() {
        return shardStatistics.size();
    }

    @Override
    public List<Replacement> replacements() {
        return Collections.singletonList(Replacement.create("shard_count", shardCount()));
    }

    @Override
    public void appendTo(EmbedBuilder embedBuilder) {
        for (var shard : shardStatistics) {
            embedBuilder.addField("#" + shard.shard(),
                    "Status: " + shard.status().name() + "\nGuilds:" + shard.guilds(), true);
        }
    }
}
