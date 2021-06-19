package de.chojo.repbot.statistic.display;

import de.chojo.repbot.statistic.EmbedDisplay;
import de.chojo.repbot.statistic.element.DataStatistic;
import de.chojo.repbot.statistic.element.ShardCountStatistic;
import net.dv8tion.jda.api.EmbedBuilder;

public class SystemInfoStatisticDisplay implements EmbedDisplay {

    private final ShardCountStatistic shardCountStatistic;
    private final DataStatistic dataStatistic;

    public SystemInfoStatisticDisplay(
            ShardCountStatistic shardCountStatistic,
            DataStatistic dataStatistic) {
        this.shardCountStatistic = shardCountStatistic;
        this.dataStatistic = dataStatistic;
    }

    @Override
    public void appendTo(EmbedBuilder embedBuilder) {
        embedBuilder.setTitle("System Info")
                .appendDescription(
                        String.format("Watching %s guilds on %s shard/s",
                                dataStatistic.guilds(), shardCountStatistic.shardCount()));
    }
}
