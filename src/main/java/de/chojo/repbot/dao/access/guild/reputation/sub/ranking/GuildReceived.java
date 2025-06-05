/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.ranking;

import de.chojo.repbot.dao.access.guild.reputation.sub.Rankings;
import de.chojo.repbot.dao.snapshots.RankingEntry;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class GuildReceived extends GuildRanking {
    public GuildReceived(Rankings rankings) {
        super(rankings, RankingType.RECEIVED);
    }

    protected int pages(int pageSize, String table) {
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

    @Override
    protected List<RankingEntry> getRankingPage(int pageSize, int page, String table) {
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
                .map(RankingEntry::buildReceivedRanking)
                .all();
    }
}
