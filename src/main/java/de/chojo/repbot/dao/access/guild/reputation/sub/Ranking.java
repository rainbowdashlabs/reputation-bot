/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub;

import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.pagination.GuildRanking;
import de.chojo.repbot.dao.snapshots.RepProfile;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Ranking implements GuildHolder {
    private final Reputation reputation;

    public Ranking(Reputation reputation) {
        this.reputation = reputation;
    }

    private int getRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation");
    }

    private Integer get7DaysRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_7_days");
    }

    private Integer get30DaysRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_30_days");
    }

    private Integer getWeekRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_week");
    }

    private Integer getMonthRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_month");
    }

    private Integer pages(int pageSize, String table) {
        return query("""
                SELECT
                    ceil(count(1)::NUMERIC / ?) AS count
                FROM
                    %s
                WHERE guild_id = ?
                    AND reputation != 0;
                """, table)
                .single(call().bind(pageSize).bind(guildId()))
                .map(row -> row.getInt("count"))
                .first()
                .orElse(1);
    }

    public GuildRanking defaultRanking(int pageSize) {
        return byMode(reputation.repGuild().settings().general().reputationMode(), pageSize);
    }

    public GuildRanking byMode(ReputationMode mode, int pageSize) {
        return switch (mode) {
            case TOTAL -> total(pageSize);
            case ROLLING_WEEK -> days7(pageSize);
            case ROLLING_MONTH -> days30(pageSize);
            case WEEK -> week(pageSize);
            case MONTH -> month(pageSize);
            default -> throw new IllegalArgumentException("Unkown input " + mode);
        };
    }

    /**
     * Get the ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking total(int pageSize) {
        return new GuildRanking("command.top.message.total", () -> getRankingPageCount(pageSize), page -> getRankingPage(pageSize, page));
    }

    /**
     * Get the 7 days ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking days7(int pageSize) {
        return new GuildRanking("command.top.message.rollingweektitle", () -> get7DaysRankingPageCount(pageSize), page -> get7DaysRankingPage(pageSize, page));
    }

    /**
     * Get the 30 days ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking days30(int pageSize) {
        return new GuildRanking("command.top.message.rollingmonthtitle", () -> get30DaysRankingPageCount(pageSize), page -> get30DaysRankingPage(pageSize, page));
    }

    /**
     * Get the weekly ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking week(int pageSize) {
        return new GuildRanking("command.top.message.weekTitle", () -> getWeekRankingPageCount(pageSize), page -> getWeekRankingPage(pageSize, page));
    }

    /**
     * Get the monthly ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking month(int pageSize) {
        return new GuildRanking("command.top.message.monthTitle", () -> getMonthRankingPageCount(pageSize), page -> getMonthRankingPage(pageSize, page));
    }

    /**
     * Get the ranking of the guild.
     *
     * @param pageSize the size of a page
     * @param page     the number of the page. zero based
     * @return a sorted list of reputation users
     */
    private List<RepProfile> getRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation");
    }

    private List<RepProfile> get7DaysRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_7_days");
    }

    private List<RepProfile> get30DaysRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_30_days");
    }

    private List<RepProfile> getWeekRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_week");
    }

    private List<RepProfile> getMonthRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_month");
    }

    private List<RepProfile> getRankingPage(int pageSize, int page, String table) {
        return query("""
                SELECT
                    rank,
                    user_id,
                    reputation
                FROM
                    %s
                WHERE guild_id = ?
                    AND reputation != 0
                ORDER BY reputation DESC
                OFFSET ?
                LIMIT ?;
                """, table)
                .single(call().bind(guildId()).bind(page * pageSize).bind(pageSize))
                .map(RepProfile::buildReceivedRanking)
                .all();
    }

    @Override
    public Guild guild() {
        return reputation.guild();
    }

    @Override
    public long guildId() {
        return reputation.guildId();
    }
}
