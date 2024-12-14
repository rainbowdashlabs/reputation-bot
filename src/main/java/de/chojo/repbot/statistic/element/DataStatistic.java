/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.statistic.element;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.statistic.ReplacementProvider;

import java.util.List;

/**
 * Class representing data statistics for the application.
 */
public class DataStatistic implements ReplacementProvider {
    private final int guilds;
    private final int activeGuilds;
    private final int activeChannel;
    private final int channel;
    private final int totalRep;
    private final int todayRep;
    private final int weeklyRep;
    private final int weeklyAvgRep;

    /**
     * Constructs a DataStatistic instance with default values.
     */
    public DataStatistic() {
        this(0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Constructs a DataStatistic instance with the specified values.
     *
     * @param guilds       the number of guilds
     * @param activeGuilds the number of active guilds
     * @param activeChannel the number of active channels
     * @param channel      the number of channels
     * @param totalRep     the total reputation
     * @param todayRep     the reputation for today
     * @param weeklyRep    the weekly reputation
     * @param weeklyAvgRep the weekly average reputation
     */
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

    /**
     * Returns the number of guilds.
     *
     * @return the number of guilds
     */
    public int guilds() {
        return guilds;
    }

    /**
     * Returns the number of channels.
     *
     * @return the number of channels
     */
    public int channel() {
        return channel;
    }

    /**
     * Returns the total reputation.
     *
     * @return the total reputation
     */
    public int totalRep() {
        return totalRep;
    }

    /**
     * Returns the reputation for today.
     *
     * @return the reputation for today
     */
    public int today() {
        return todayRep;
    }

    /**
     * Returns the weekly reputation.
     *
     * @return the weekly reputation
     */
    public int weeklyRep() {
        return weeklyRep;
    }

    /**
     * Returns the weekly average reputation.
     *
     * @return the weekly average reputation
     */
    public int weeklyAvgRep() {
        return weeklyAvgRep;
    }

    /**
     * Returns the number of active guilds.
     *
     * @return the number of active guilds
     */
    public int activeGuilds() {
        return activeGuilds;
    }

    /**
     * Returns the number of active channels.
     *
     * @return the number of active channels
     */
    public int activeChannel() {
        return activeChannel;
    }

    /**
     * Returns a list of replacements for localization.
     *
     * @return a list of {@link Replacement} objects
     */
    @Override
    public List<Replacement> replacements() {
        return List.of(Replacement.create("guild_count", guilds), Replacement.create("channel_count", channel),
                Replacement.create("active_guilds", activeGuilds), Replacement.create("active_channel", activeChannel),
                Replacement.create("total_rep", totalRep), Replacement.create("today_rep", todayRep),
                Replacement.create("weekly_rep", weeklyRep), Replacement.create("weekly_avg_rep", weeklyAvgRep));
    }
}
