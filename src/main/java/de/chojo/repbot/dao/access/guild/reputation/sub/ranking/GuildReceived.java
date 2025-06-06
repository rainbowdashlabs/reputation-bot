/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.ranking;

import de.chojo.repbot.dao.access.guild.reputation.sub.Rankings;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.snapshots.RankingEntry;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class GuildReceived extends GuildRanking {
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
                  AND (received > :reset_date OR :reset_date IS NULL)
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
        return query("""
                WITH
                    rep_offset
                        AS (
                        SELECT
                            o.user_id,
                            sum(o.amount) AS reputation
                        FROM
                            reputation_offset o
                        WHERE o.added > :date_init
                          AND o.guild_id = :guild_id
                          AND ( added > :reset_date OR :reset_date IS NULL )
                        GROUP BY o.user_id
                           ),
                    full_log
                        AS (
                        SELECT
                            receiver_id AS user_id,
                            count(1)    AS reputation
                        FROM
                            reputation_log
                        WHERE received > :date_init
                          AND ( received > :reset_date OR :reset_date IS NULL )
                          AND guild_id = :guild_id
                          AND receiver_id NOT IN (
                            SELECT
                                user_id
                            FROM
                                cleanup_schedule
                            WHERE guild_id = :guild_id
                                                 )
                        GROUP BY receiver_id
                           ),
                    offset_reputation
                        AS (
                        SELECT
                            coalesce(f.user_id, o.user_id)                        AS user_id,
                            -- apply offset to the normal reputation.
                            coalesce(f.reputation, 0) + coalesce(o.reputation, 0) AS reputation
                        FROM
                            full_log f
                                FULL JOIN rep_offset o
                                ON f.user_id = o.user_id
                           )
                SELECT
                    rank() OVER (ORDER BY reputation DESC) AS rank,
                    user_id,
                    reputation::BIGINT                     AS reputation
                FROM
                    offset_reputation rank
                OFFSET ? LIMIT ?;
                """)
                .single(call().bind("reset_date", resetDate())
                              .bind("guild_id", guildId())
                              .bind("date_init", mode.dateInit(), INSTANT_TIMESTAMP)
                              .bind(page * pageSize)
                              .bind(pageSize))
                .map(RankingEntry::buildReceivedRanking)
                .all();
    }
}
