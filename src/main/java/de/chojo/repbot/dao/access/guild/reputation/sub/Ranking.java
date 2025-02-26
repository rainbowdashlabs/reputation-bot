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

/**
 * Manages the ranking of users based on their reputation in a guild.
 */
public class Ranking implements GuildHolder {
    private final Reputation reputation;

    /**
     * Constructs a Ranking instance with the specified Reputation instance.
     *
     * @param reputation the Reputation instance
     */
    public Ranking(Reputation reputation) {
        this.reputation = reputation;
    }

    /**
     * Gets the total number of ranking pages.
     *
     * @param pageSize the size of a page
     * @return the total number of ranking pages
     */
    private int getRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation");
    }

    /**
     * Gets the total number of 7-day ranking pages.
     *
     * @param pageSize the size of a page
     * @return the total number of 7-day ranking pages
     */
    private Integer get7DaysRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_7_days");
    }

    /**
     * Gets the total number of 30-day ranking pages.
     *
     * @param pageSize the size of a page
     * @return the total number of 30-day ranking pages
     */
    private Integer get30DaysRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_30_days");
    }

    /**
     * Gets the total number of weekly ranking pages.
     *
     * @param pageSize the size of a page
     * @return the total number of weekly ranking pages
     */
    private Integer getWeekRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_week");
    }

    /**
     * Gets the total number of monthly ranking pages.
     *
     * @param pageSize the size of a page
     * @return the total number of monthly ranking pages
     */
    private Integer getMonthRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_month");
    }

    /**
     * Calculates the number of pages for a given table.
     *
     * @param pageSize the size of a page
     * @param table the table name
     * @return the number of pages
     */
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

    /**
     * Gets the default ranking of the guild based on the reputation mode.
     *
     * @param pageSize the size of a page
     * @return the default ranking of the guild
     */
    public GuildRanking defaultRanking(int pageSize) {
        return byMode(reputation.repGuild().settings().general().reputationMode(), pageSize);
    }

    /**
     * Gets the ranking of the guild based on the specified reputation mode.
     *
     * @param mode the reputation mode
     * @param pageSize the size of a page
     * @return the ranking of the guild
     */
    public GuildRanking byMode(ReputationMode mode, int pageSize) {
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
     * Gets the total ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking total(int pageSize) {
        return new GuildRanking("command.top.message.total", () -> getRankingPageCount(pageSize), page -> getRankingPage(pageSize, page));
    }

    /**
     * Gets the 7-day ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking days7(int pageSize) {
        return new GuildRanking("command.top.message.rollingweektitle", () -> get7DaysRankingPageCount(pageSize), page -> get7DaysRankingPage(pageSize, page));
    }

    /**
     * Gets the 30-day ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking days30(int pageSize) {
        return new GuildRanking("command.top.message.rollingmonthtitle", () -> get30DaysRankingPageCount(pageSize), page -> get30DaysRankingPage(pageSize, page));
    }

    /**
     * Gets the weekly ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking week(int pageSize) {
        return new GuildRanking("command.top.message.weekTitle", () -> getWeekRankingPageCount(pageSize), page -> getWeekRankingPage(pageSize, page));
    }

    /**
     * Gets the monthly ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking month(int pageSize) {
        return new GuildRanking("command.top.message.monthTitle", () -> getMonthRankingPageCount(pageSize), page -> getMonthRankingPage(pageSize, page));
    }

    /**
     * Gets the ranking of the guild.
     *
     * @param pageSize the size of a page
     * @param page the number of the page (zero-based)
     * @return a sorted list of reputation users
     */
    private List<RepProfile> getRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation");
    }

    /**
     * Gets the 7-day ranking page of the guild.
     *
     * @param pageSize the size of a page
     * @param page the number of the page (zero-based)
     * @return a sorted list of reputation users
     */
    private List<RepProfile> get7DaysRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_7_days");
    }

    /**
     * Gets the 30-day ranking page of the guild.
     *
     * @param pageSize the size of a page
     * @param page the number of the page (zero-based)
     * @return a sorted list of reputation users
     */
    private List<RepProfile> get30DaysRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_30_days");
    }

    /**
     * Gets the weekly ranking page of the guild.
     *
     * @param pageSize the size of a page
     * @param page the number of the page (zero-based)
     * @return a sorted list of reputation users
     */
    private List<RepProfile> getWeekRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_week");
    }

    /**
     * Gets the monthly ranking page of the guild.
     *
     * @param pageSize the size of a page
     * @param page the number of the page (zero-based)
     * @return a sorted list of reputation users
     */
    private List<RepProfile> getMonthRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_month");
    }

    /**
     * Gets the ranking page of the guild for the specified table.
     *
     * @param pageSize the size of a page
     * @param page the number of the page (zero-based)
     * @param table the table name
     * @return a sorted list of reputation users
     */
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

    /**
     * Retrieves the guild associated with this instance.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return reputation.guild();
    }

    /**
     * Retrieves the guild ID associated with this instance.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return reputation.guildId();
    }
}
