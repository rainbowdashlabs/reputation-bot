package de.chojo.repbot.dao.access.guild.reputation.sub;

import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.pagination.GuildRanking;
import de.chojo.repbot.dao.snapshots.RepProfile;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

public class Ranking extends QueryFactory implements GuildHolder {
    private final Reputation reputation;

    public Ranking(Reputation reputation) {
        super(reputation);
        this.reputation = reputation;
    }

    private int getRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation");
    }

    private Integer getWeekRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_week");
    }

    private Integer getMonthRankingPageCount(int pageSize) {
        return pages(pageSize, "user_reputation_month");
    }

    private Integer pages(int pageSize, String table) {
        return builder(Integer.class)
                .query("""
                       SELECT
                           CEIL(COUNT(1)::numeric / ?) AS count
                       FROM
                           %s
                       WHERE guild_id = ?
                           AND reputation != 0;
                       """, table)
                .parameter(stmt -> stmt.setInt(pageSize).setLong(guildId()))
                .readRow(row -> row.getInt("count"))
                .firstSync()
                .orElse(1);
    }

    public GuildRanking defaultRanking(int pageSize) {
        return byMode(reputation.repGuild().settings().general().reputationMode(), pageSize);
    }

    public GuildRanking byMode(ReputationMode mode, int pageSize) {
        return switch (mode) {
            case TOTAL -> total(pageSize);
            case ROLLING_WEEK -> week(pageSize);
            case ROLLING_MONTH -> month(pageSize);
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

    private List<RepProfile> getWeekRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_week");
    }

    private List<RepProfile> getMonthRankingPage(int pageSize, int page) {
        return getRankingPage(pageSize, page, "user_reputation_month");
    }

    private List<RepProfile> getRankingPage(int pageSize, int page, String table) {
        return builder(RepProfile.class)
                .query("""
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
                .parameter(stmt -> stmt.setLong(guildId()).setInt(page * pageSize).setInt(pageSize))
                .readRow(RepProfile::buildReceivedRanking)
                .allSync();
    }

    @Override
    public Guild guild() {
        return reputation.guild();
    }
}
