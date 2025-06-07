/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.ranking;

import de.chojo.repbot.dao.access.guild.reputation.sub.Rankings;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.snapshots.RankingEntry;
import de.chojo.repbot.util.QueryLoader;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class GuildReceived extends GuildRanking {
    private static final String RANKING = QueryLoader.loadQuery("ranking", "guild", "received");
    public GuildReceived(Rankings rankings) {
        super(rankings, RankingType.RECEIVED);
    }

    protected int pages(int pageSize, ReputationMode mode) {
        return query("""
                SELECT
                    ceil(count(1)::NUMERIC / ?) AS count
                FROM
                    reputation_log
                WHERE guild_id = :guild_id
                  AND received > :date_init
                  AND receiver_id NOT IN (SELECT user_id FROM cleanup_schedule WHERE guild_id = :guild_id)
                  AND (received > :reset_date OR :reset_date::TIMESTAMP IS NULL)
                GROUP BY receiver_id;
                """)
                .single(call().bind("reset_date", resetDate())
                              .bind(pageSize)
                              .bind("guild_id", guildId())
                              .bind("date_init", mode.dateInit(), INSTANT_TIMESTAMP))
                .map(row -> row.getInt("count"))
                .first()
                .orElse(1);
    }

    @Override
    protected List<RankingEntry> getRankingPage(int pageSize, int page, ReputationMode mode) {
        return query(RANKING)
                .single(call().bind("reset_date", resetDate())
                              .bind("guild_id", guildId())
                              .bind("date_init", mode.dateInit(), INSTANT_TIMESTAMP)
                              .bind(page * pageSize)
                              .bind(pageSize))
                .map(RankingEntry::buildReceivedRanking)
                .all();
    }
}
