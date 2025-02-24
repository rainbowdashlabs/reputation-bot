/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.statistic.display;

import de.chojo.repbot.statistic.EmbedDisplay;
import de.chojo.repbot.statistic.element.DataStatistic;
import de.chojo.repbot.statistic.element.ShardCountStatistic;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Class representing the system information statistic display.
 *
 * @param shardCountStatistic the shard count statistic
 * @param dataStatistic the data statistic
 */
public record SystemInfoStatisticDisplay(ShardCountStatistic shardCountStatistic,
                                         DataStatistic dataStatistic) implements EmbedDisplay {

    /**
     * Appends the system information statistics to the given EmbedBuilder.
     *
     * @param embedBuilder the EmbedBuilder to append to
     */
    @Override
    public void appendTo(EmbedBuilder embedBuilder) {
        embedBuilder.setTitle("System Info")
                    .appendDescription(
                            String.format("Watching %s guilds on %s shard/s\n%s/%s active channel on %s active guilds.",
                                    dataStatistic.guilds(), shardCountStatistic.shardCount(),
                                    dataStatistic.activeChannel(), dataStatistic.channel(), dataStatistic.activeGuilds()));
    }
}
