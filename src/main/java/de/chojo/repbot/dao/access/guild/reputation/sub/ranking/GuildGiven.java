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

public class GuildGiven extends GuildRanking {
    public GuildGiven(Rankings rankings) {
        super(rankings, RankingType.GIVEN);
    }

    @Override
    protected int pages(int pageSize, ReputationMode mode) {
        return query("""
                SELECT
                    ceil(count(1)::NUMERIC / ?) AS count
                FROM
                    (
                        SELECT
                            DISTINCT donor_id
                        FROM
                            reputation_log
                        WHERE guild_id = :guild_id
                          AND donor_id IS NOT NULL
                          AND donor_id NOT IN (
                            SELECT user_id
                            FROM cleanup_schedule
                            WHERE guild_id = :guild_id
                                              )
                          AND ( received > :reset_date OR :reset_date::TIMESTAMP IS NULL )
                          AND received >= :date_init
                    ) a;
                """)
                .single(call().bind("reset_date", resetDate())
                              .bind(pageSize)
                              .bind("guild_id", guildId()
                              ).bind("date_init",mode.dateInit(), INSTANT_TIMESTAMP))
                .map(row -> row.getInt("count"))
                .first()
                .orElse(0);
    }

    @Override
    protected List<RankingEntry> getRankingPage(int pageSize, int page, ReputationMode mode) {
        return query("""
                WITH
                    full_log
                        AS (
                        SELECT
                            r.guild_id,
                            r.donor_id,
                            count(1) AS donated
                        FROM
                            reputation_log r
                        WHERE r.received > :date_init
                          AND (received > :reset_date OR :reset_date::TIMESTAMP IS NULL)
                          AND guild_id = :guild_id
                          AND donor_id IS NOT NULL
                          AND donor_id NOT IN (SELECT user_id FROM cleanup_schedule WHERE r.guild_id = :guild_id)
                        GROUP BY r.guild_id, r.donor_id
                           )
                SELECT
                    rank() OVER (ORDER BY donor_id DESC) AS rank_donated,
                    donor_id                             AS user_id,
                    donated
                FROM
                    full_log
                OFFSET ?
                LIMIT ?;
                """)
                .single(call().bind("reset_date", resetDate())
                              .bind("date_init",mode.dateInit(), INSTANT_TIMESTAMP)
                              .bind("guild_id", guildId())
                              .bind(page * pageSize)
                              .bind(pageSize))
                .map(RankingEntry::buildGivenRanking)
                .all();
    }
}
