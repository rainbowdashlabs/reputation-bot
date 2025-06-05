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

public class UserGiven extends UserRanking {
    public UserGiven(UserRankings user) {
        super(user, RankingType.GIVEN);
    }

    @Override
    protected List<RankingEntry> getRankingPage(int pageSize, int page, Member member, ReputationMode mode) {
        return query("""
                WITH
                    counts AS (
                        SELECT
                            receiver_id,
                            count(1) AS count
                        FROM
                            reputation_log
                        WHERE guild_id = ?
                          AND donor_id = ?
                          AND received >= ?
                        GROUP BY guild_id, receiver_id
                    )
                SELECT
                    rank() OVER (ORDER BY count) as rank_donated,
                    receiver_id as user_id,
                    count as donated
                FROM
                    counts;
                """)
                .single(call().bind(guildId()).bind(member.getIdLong()).bind(mode.dateInit(), StandardValueConverter.INSTANT_TIMESTAMP))
                .map(RankingEntry::buildGivenRanking)
                .all();
    }

    @Override
    protected int pages(int pageSize, Member member, ReputationMode mode) {
        return query("""
                SELECT
                    ceil(count(1)::NUMERIC / ?) AS count
                FROM
                    reputation_log
                WHERE guild_id = ?
                  AND donor_id = ?
                  AND received >= ?
                GROUP BY receiver_id;
                """)
                .single(call().bind(pageSize).bind(guildId()).bind(member.getIdLong()).bind(mode.dateInit(), StandardValueConverter.INSTANT_TIMESTAMP))
                .map(row -> row.getInt("count"))
                .first()
                .orElse(1);
    }
}
