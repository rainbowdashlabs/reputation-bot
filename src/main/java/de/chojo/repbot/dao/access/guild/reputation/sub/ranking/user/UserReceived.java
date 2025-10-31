/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.ranking.user;

import de.chojo.repbot.dao.access.guild.reputation.sub.ranking.RankingType;
import de.chojo.repbot.dao.access.guild.reputation.sub.ranking.UserRankings;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.snapshots.RankingEntry;
import de.chojo.sadu.queries.converter.StandardValueConverter;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class UserReceived extends UserRanking {
    public UserReceived(UserRankings user) {
        super(user, RankingType.RECEIVED);
    }

    @Override
    protected List<RankingEntry> getRankingPage(int pageSize, int page, Member member, ReputationMode mode) {
        return query("""
                WITH
                    counts AS (
                        SELECT
                            donor_id,
                            count(1) AS count
                        FROM
                            reputation_log
                        WHERE guild_id = ?
                          AND receiver_id = ?
                          AND received >= ?
                          AND donor_id IS NOT NULL
                          AND (received > :reset_date OR :reset_date::TIMESTAMP IS NULL)
                        GROUP BY guild_id, donor_id
                        ORDER BY count DESC
                    )
                SELECT
                    rank() OVER (ORDER BY count DESC) AS rank,
                    donor_id AS user_id,
                    count AS reputation
                FROM
                    counts
                OFFSET ?
                LIMIT ?;
                """)
                .single(call().bind(guildId()).bind("reset_date", resetDate(), INSTANT_TIMESTAMP)
                              .bind(member.getIdLong())
                              .bind(mode.dateInit(), INSTANT_TIMESTAMP)
                              .bind(page * pageSize)
                              .bind(pageSize))
                .map(RankingEntry::buildReceivedRanking)
                .all();
    }

    @Override
    protected int pages(int pageSize, Member member, ReputationMode mode) {
        return query("""
                SELECT
                    ceil(count(1)::NUMERIC / ?) AS count
                FROM
                    (
                        SELECT DISTINCT
                            donor_id
                        FROM
                            reputation_log
                        WHERE guild_id = ?
                          AND receiver_id = ?
                          AND received >= ?
                          AND donor_id IS NOT NULL
                          AND ( received > :reset_date OR :reset_date::TIMESTAMP IS NULL )
                    ) a""")
                .single(call().bind(pageSize).bind(guildId())
                              .bind("reset_date", resetDate(), INSTANT_TIMESTAMP)
                              .bind(member.getIdLong())
                              .bind(mode.dateInit(), INSTANT_TIMESTAMP))
                .map(row -> row.getInt("count"))
                .first()
                .orElse(0);
    }
}
