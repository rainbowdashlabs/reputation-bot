/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.vote;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class VoteStreak {
    private long userId;
    private String botlist;
    private Instant lastVote;
    private int streak;

    public VoteStreak(long userId, String botlist, Instant lastVote, int streak) {
        this.userId = userId;
        this.botlist = botlist;
        this.lastVote = lastVote;
        this.streak = streak;
    }

    @MappingProvider({"user_id", "botlist", "last_vote", "streak"})
    public VoteStreak(Row row) throws SQLException {
        this(
                row.getLong("user_id"),
                row.getString("botlist"),
                row.get("last_vote", INSTANT_TIMESTAMP),
                row.getInt("streak"));
    }

    public long userId() {
        return userId;
    }

    public String botlist() {
        return botlist;
    }

    public Instant lastVote() {
        return lastVote;
    }

    public int streak() {
        return streak;
    }

    public void incrementStreak() {
        Optional<VoteStreak> change = query("""
                INSERT
                INTO
                    votes
                    (user_id, botlist, last_vote, streak)
                VALUES
                    (?, ?, now(), 1)
                ON CONFLICT(user_id, botlist)
                    DO UPDATE
                    SET
                        last_vote = now(),
                        streak    = streak + 1,
                        votes     = votes + 1
                RETURNING last_vote, streak
                """)
                .single(call().bind(userId).bind(botlist))
                .map(rs -> new VoteStreak(userId, botlist, rs.get("last_vote", INSTANT_TIMESTAMP), rs.getInt("streak")))
                .first();
        change.ifPresent(vote -> {
            lastVote = vote.lastVote();
            streak = vote.streak();
        });
    }

    public void resetStreak() {
        Optional<VoteStreak> change = query("""
                INSERT
                INTO
                    votes(user_id, botlist, last_vote, streak)
                VALUES
                    (?, ?, ?, ?)
                ON CONFLICT (user_id, botlist)
                    DO UPDATE
                    SET
                        streak    = 0,
                        last_vote = now(),
                        votes     = votes + 1
                RETURNING last_vote, streak;
                """)
                .single(call().bind(userId()).bind(botlist))
                .map(rs -> new VoteStreak(userId, botlist, rs.get("last_vote", INSTANT_TIMESTAMP), rs.getInt("streak")))
                .first();
        change.ifPresent(vote -> {
            lastVote = vote.lastVote();
            streak = vote.streak();
        });
    }
}
