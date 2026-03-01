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
    private Instant streakStart;
    private int streakDays;

    public VoteStreak(long userId, String botlist, Instant lastVote, int streak, Instant streakStart, int streakDays) {
        this.userId = userId;
        this.botlist = botlist;
        this.lastVote = lastVote;
        this.streak = streak;
        this.streakStart = streakStart;
        this.streakDays = streakDays;
    }

    @MappingProvider({"user_id", "botlist", "last_vote", "streak"})
    public VoteStreak(Row row) throws SQLException {
        this(
                row.getLong("user_id"),
                row.getString("botlist"),
                row.get("last_vote", INSTANT_TIMESTAMP),
                row.getInt("streak"),
                row.get("streak_start", INSTANT_TIMESTAMP),
                row.getInt("streak_days"));
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

    public Instant streakStart() {
        return streakStart;
    }

    public int streakDays() {
        return streakDays;
    }

    public void incrementStreak() {
        Optional<VoteStreak> change = query("""
                INSERT
                INTO
                    votes AS v
                    (user_id, botlist, last_vote, streak)
                VALUES
                    (?, ?, now(), 0)
                ON CONFLICT(user_id, botlist)
                    DO UPDATE
                    SET
                        last_vote = now(),
                        streak    = v.streak + 1,
                        votes     = v.votes + 1
                RETURNING last_vote, streak, streak_start, streak_days;
                """)
                .single(call().bind(userId).bind(botlist))
                .map(rs -> new VoteStreak(
                        userId,
                        botlist,
                        rs.get("last_vote", INSTANT_TIMESTAMP),
                        rs.getInt("streak"),
                        rs.get("streak_start", INSTANT_TIMESTAMP),
                        rs.getInt("streak_days")))
                .first();
        change.ifPresent(vote -> {
            lastVote = vote.lastVote();
            streak = vote.streak();
            streakDays = vote.streakDays();
        });
    }

    public void resetStreak() {
        Optional<VoteStreak> change = query("""
                INSERT
                INTO
                    votes AS v(user_id, botlist, last_vote, streak)
                VALUES
                    (?, ?, now(), 0)
                ON CONFLICT (user_id, botlist)
                    DO UPDATE
                    SET
                        streak    = 0,
                        last_vote = now(),
                        votes     = v.votes + 1,
                        streak_start = now()
                RETURNING last_vote, streak, streak_start, streak_days;
                """)
                .single(call().bind(userId()).bind(botlist))
                .map(rs -> new VoteStreak(
                        userId,
                        botlist,
                        rs.get("last_vote", INSTANT_TIMESTAMP),
                        rs.getInt("streak"),
                        rs.get("streak_start", INSTANT_TIMESTAMP),
                        rs.getInt("streak_days")))
                .first();
        change.ifPresent(vote -> {
            lastVote = vote.lastVote();
            streak = vote.streak();
            streakDays = vote.streakDays();
            streakStart = vote.streakStart();
        });
    }
}
