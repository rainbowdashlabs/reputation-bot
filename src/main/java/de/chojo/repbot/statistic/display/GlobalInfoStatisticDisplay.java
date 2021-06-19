package de.chojo.repbot.statistic.display;

import de.chojo.repbot.statistic.EmbedDisplay;
import de.chojo.repbot.statistic.element.DataStatistic;
import de.chojo.repbot.statistic.element.GlobalShardStatistic;
import net.dv8tion.jda.api.EmbedBuilder;

public class GlobalInfoStatisticDisplay implements EmbedDisplay {

    private final GlobalShardStatistic globalShardStatistic;
    private final DataStatistic dataStatistic;

    public GlobalInfoStatisticDisplay(
            GlobalShardStatistic globalShardStatistic,
            DataStatistic dataStatistic) {
        this.globalShardStatistic = globalShardStatistic;
        this.dataStatistic = dataStatistic;
    }


    @Override
    public void appendTo(EmbedBuilder embedBuilder) {
        embedBuilder.addField("Global Info",
                String.format("""
                                Analyzed: %s
                                Total Reputation: %s
                                Week Reputation: %s
                                AverageWeek Reputation: %s
                                Today Reputation: %s
                                """.stripIndent(),
                        globalShardStatistic.analyzedMessages(), dataStatistic.totalRep(), dataStatistic.weeklyRep(),
                        dataStatistic.weeklyAvgRep(), dataStatistic.today()), false);
    }
}
