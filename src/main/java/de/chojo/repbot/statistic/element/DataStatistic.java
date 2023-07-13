/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.statistic.element;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.statistic.ReplacementProvider;

import java.util.List;

public class DataStatistic implements ReplacementProvider {
    private final int guilds;
    private final int activeGuilds;
    private final int activeChannel;
    private final int channel;
    private final int totalRep;
    private final int todayRep;
    private final int weeklyRep;
    private final int weeklyAvgRep;

    public DataStatistic() {
        this(0, 0, 0, 0, 0, 0, 0, 0);
    }

    public DataStatistic(int guilds, int activeGuilds, int activeChannel, int channel, int totalRep, int todayRep, int weeklyRep, int weeklyAvgRep) {
        this.guilds = guilds;
        this.activeGuilds = activeGuilds;
        this.activeChannel = activeChannel;
        this.channel = channel;
        this.totalRep = totalRep;
        this.todayRep = todayRep;
        this.weeklyRep = weeklyRep;
        this.weeklyAvgRep = weeklyAvgRep;
    }

    public int guilds() {
        return guilds;
    }

    public int channel() {
        return channel;
    }

    public int totalRep() {
        return totalRep;
    }

    public int today() {
        return todayRep;
    }

    public int weeklyRep() {
        return weeklyRep;
    }

    public int weeklyAvgRep() {
        return weeklyAvgRep;
    }

    public int activeGuilds() {
        return activeGuilds;
    }

    public int activeChannel() {
        return activeChannel;
    }

    @Override
    public List<Replacement> replacements() {
        return List.of(Replacement.create("guild_count", guilds), Replacement.create("channel_count", channel),
                Replacement.create("active_guilds", activeGuilds), Replacement.create("active_channel", activeChannel),
                Replacement.create("total_rep", totalRep), Replacement.create("today_rep", todayRep),
                Replacement.create("weekly_rep", weeklyRep), Replacement.create("weekly_avg_rep", weeklyAvgRep));
    }
}
