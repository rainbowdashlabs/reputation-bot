/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.ranking;

import de.chojo.repbot.dao.access.guild.reputation.sub.Ranking;
import de.chojo.repbot.dao.snapshots.RankingEntry;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class GuildGiven extends GuildRanking {
    public GuildGiven(Ranking ranking) {
        super(ranking);
    }


    protected int pages(int pageSize, String table) {
        return query("""
                SELECT
                    ceil(count(1)::NUMERIC / ?) AS count
                FROM
                    %s
                WHERE guild_id = ?
                    AND donated != 0;
                """, table)
                .single(call().bind(pageSize).bind(guildId()))
                .map(row -> row.getInt("count"))
                .first()
                .orElse(1);
    }


    protected List<RankingEntry> getRankingPage(int pageSize, int page, String table) {
        return query("""
                SELECT
                    rank_donated,
                    user_id,
                    donated
                FROM
                    %s
                WHERE guild_id = ?
                    AND donated != 0
                ORDER BY donated DESC
                OFFSET ?
                LIMIT ?;
                """, table)
                .single(call().bind(guildId()).bind(page * pageSize).bind(pageSize))
                .map(RankingEntry::buildGivenRanking)
                .all();
    }
}
