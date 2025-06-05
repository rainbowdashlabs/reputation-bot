/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.ranking;

import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.pagination.Ranking;
import de.chojo.repbot.dao.snapshots.RankingEntry;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

import static de.chojo.jdautil.localization.util.Replacement.create;

public abstract class GuildRanking implements GuildHolder {
    private final de.chojo.repbot.dao.access.guild.reputation.sub.Ranking ranking;

    protected GuildRanking(de.chojo.repbot.dao.access.guild.reputation.sub.Ranking ranking) {
        this.ranking = ranking;
    }

    @Override
    public Guild guild() {
        return ranking.guild();
    }

    @Override
    public long guildId() {
        return ranking.guildId();
    }

    public Ranking defaultRanking(int pageSize) {
        return byMode(ranking.reputation().repGuild().settings().general().reputationMode(), pageSize);
    }

    public Ranking byMode(ReputationMode mode, int pageSize) {
        return switch (mode) {
            case TOTAL -> total(pageSize);
            case ROLLING_WEEK -> days7(pageSize);
            case ROLLING_MONTH -> days30(pageSize);
            case WEEK -> week(pageSize);
            case MONTH -> month(pageSize);
            default -> throw new IllegalArgumentException("Unknown input " + mode);
        };
    }

    /**
     * Get the ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public Ranking total(int pageSize) {
        return new Ranking("command.top.message.total", create("GUILD", guild().getName()), () -> getRankingPageCount(pageSize), page -> getRankingPage(pageSize, page));
    }

    /**
     * Get the 7 days ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public Ranking days7(int pageSize) {
        return new Ranking("command.top.message.rollingweektitle", create("GUILD", guild().getName()), () -> get7DaysRankingPageCount(pageSize), page -> get7DaysRankingPage(pageSize, page));
    }

    /**
     * Get the 30 days ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public Ranking days30(int pageSize) {
        return new Ranking("command.top.message.rollingmonthtitle", create("GUILD", guild().getName()), () -> get30DaysRankingPageCount(pageSize), page -> get30DaysRankingPage(pageSize, page));
    }

    /**
     * Get the weekly ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public Ranking week(int pageSize) {
        return new Ranking("command.top.message.weekTitle", create("GUILD", guild().getName()), () -> getWeekRankingPageCount(pageSize), page -> getWeekRankingPage(pageSize, page));
    }

    /**
     * Get the monthly ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public Ranking month(int pageSize) {
        return new Ranking("command.top.message.monthTitle", create("GUILD", guild().getName()), () -> getMonthRankingPageCount(pageSize), page -> getMonthRankingPage(pageSize, page));
    }


    private int getRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation");
    }

    private int get7DaysRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_7_days");
    }

    private int get30DaysRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_30_days");
    }

    private int getWeekRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_week");
    }

    private int getMonthRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_month");
    }

    /**
     * Get the ranking of the guild.
     *
     * @param pageSize the size of a page
     * @param page     the number of the page. zero based
     * @return a sorted list of reputation users
     */
    private List<RankingEntry> getRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation");
    }

    private List<RankingEntry> get7DaysRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_7_days");
    }

    private List<RankingEntry> get30DaysRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_30_days");
    }

    private List<RankingEntry> getWeekRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_week");
    }

    private List<RankingEntry> getMonthRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_month");
    }

    protected abstract List<RankingEntry> getRankingPage(int pageSize, int page, String table);

    protected abstract int pages(int pageSize, String table);
}
