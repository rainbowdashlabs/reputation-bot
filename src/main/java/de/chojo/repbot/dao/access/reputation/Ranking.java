package de.chojo.repbot.dao.access.reputation;

import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.snapshots.ReputationUser;
import de.chojo.repbot.data.wrapper.GuildRanking;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

public class Ranking extends QueryFactoryHolder implements GuildHolder {
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
                .paramsBuilder(stmt -> stmt.setInt(pageSize).setLong(guildId()))
                .readRow(row -> row.getInt("count"))
                .firstSync()
                .orElse(1);
    }

    /**
     * Get the ranking of the guild.
     *
     * @param guild    guild
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking getRanking(Guild guild, int pageSize) {
        return new GuildRanking(() -> getRankingPageCount(pageSize), page -> getRankingPage(guild, pageSize, page));
    }

    /**
     * Get the weekly ranking of the guild.
     *
     * @param guild    guild
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking getWeekRanking(Guild guild, int pageSize) {
        return new GuildRanking(() -> getWeekRankingPageCount(pageSize), page -> getWeekRankingPage(guild, pageSize, page));
    }

    /**
     * Get the monthly ranking of the guild.
     *
     * @param guild    guild
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public GuildRanking getMonthRanking(Guild guild, int pageSize) {
        return new GuildRanking(() -> getMonthRankingPageCount(pageSize), page -> getMonthRankingPage(guild, pageSize, page));
    }

    /**
     * Get the ranking of the guild.
     *
     * @param guild    guild
     * @param pageSize the size of a page
     * @param page     the number of the page. zero based
     * @return a sorted list of reputation users
     */
    private List<ReputationUser> getRankingPage(Guild guild, int pageSize, int page) {
        return getRankingPage(guild, pageSize, page, "user_reputation");
    }

    private List<ReputationUser> getWeekRankingPage(Guild guild, int pageSize, int page) {
        return getRankingPage(guild, pageSize, page, "user_reputation_week");
    }

    private List<ReputationUser> getMonthRankingPage(Guild guild, int pageSize, int page) {
        return getRankingPage(guild, pageSize, page, "user_reputation_month");
    }

    private List<ReputationUser> getRankingPage(Guild guild, int pageSize, int page, String table) {
        return builder(ReputationUser.class)
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
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setInt(page * pageSize).setInt(pageSize))
                .readRow(row -> new ReputationUser(row.getLong("rank"), row.getLong("user_id"), row.getLong("reputation")))
                .allSync();
    }

    @Override
    public Guild guild() {
        return reputation.guild();
    }
}
