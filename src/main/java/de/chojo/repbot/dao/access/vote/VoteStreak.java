/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.vote;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class VoteStreak {
    private final long userId;
    private final String botlist;
    private Instant lastVote;
    private int streak;
    private Instant streakStart;
    private int streakDays;
    private boolean reminder;
    private Instant reminderTimestamp;
    private boolean reminderSent;

    public VoteStreak(
            long userId,
            String botlist,
            Instant lastVote,
            int streak,
            Instant streakStart,
            int streakDays,
            boolean reminder,
            Instant reminderTimestamp,
            boolean sent) {
        this.userId = userId;
        this.botlist = botlist;
        this.lastVote = lastVote;
        this.streak = streak;
        this.streakStart = streakStart;
        this.streakDays = streakDays;
        this.reminder = reminder;
        this.reminderTimestamp = reminderTimestamp;
        this.reminderSent = sent;
    }

    @MappingProvider({
        "user_id",
        "botlist",
        "last_vote",
        "streak",
        "streak_start",
        "reminder",
        "reminder_timestamp",
        "sent"
    })
    public VoteStreak(Row row) throws SQLException {
        this(
                row.getLong("user_id"),
                row.getString("botlist"),
                row.get("last_vote", INSTANT_TIMESTAMP),
                row.getInt("streak"),
                row.get("streak_start", INSTANT_TIMESTAMP),
                row.getInt("streak_days"),
                row.getBoolean("reminder"),
                row.get("reminder_timestamp", INSTANT_TIMESTAMP),
                row.getBoolean("sent"));
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
                    (user_id, botlist, last_vote, streak, reminder_timestamp)
                VALUES
                    (?, ?, now(), 0, now() +  '12 hours'::INTERVAL)
                ON CONFLICT(user_id, botlist)
                    DO UPDATE
                    SET
                        last_vote = now(),
                        streak    = v.streak + 1,
                        votes     = v.votes + 1,
                        reminder_timestamp = now() + '12 hours'::INTERVAL,
                        sent = false
                RETURNING last_vote, streak, streak_start, streak_days, reminder, reminder_timestamp, sent;
                """)
                .single(call().bind(userId).bind(botlist))
                .map(row -> new VoteStreak(
                        userId,
                        botlist,
                        row.get("last_vote", INSTANT_TIMESTAMP),
                        row.getInt("streak"),
                        row.get("streak_start", INSTANT_TIMESTAMP),
                        row.getInt("streak_days"),
                        row.getBoolean("reminder"),
                        row.get("reminder_timestamp", INSTANT_TIMESTAMP),
                        row.getBoolean("sent")))
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
                    votes AS v(user_id, botlist, last_vote, streak, reminder_timestamp)
                VALUES
                    (?, ?, now(), 0, now() + '12 hours'::INTERVAL)
                ON CONFLICT (user_id, botlist)
                    DO UPDATE
                    SET
                        streak    = 0,
                        last_vote = now(),
                        votes     = v.votes + 1,
                        streak_start = now(),
                        reminder_timestamp = now() + '12 hours'::INTERVAL,
                        sent = false
                RETURNING last_vote, streak, streak_start, streak_days, reminder, reminder_timestamp, sent;
                """)
                .single(call().bind(userId()).bind(botlist))
                .map(row -> new VoteStreak(
                        userId,
                        botlist,
                        row.get("last_vote", INSTANT_TIMESTAMP),
                        row.getInt("streak"),
                        row.get("streak_start", INSTANT_TIMESTAMP),
                        row.getInt("streak_days"),
                        row.getBoolean("reminder"),
                        row.get("reminder_timestamp", INSTANT_TIMESTAMP),
                        row.getBoolean("sent")))
                .first();
        change.ifPresent(vote -> {
            lastVote = vote.lastVote();
            streak = vote.streak();
            streakDays = vote.streakDays();
            streakStart = vote.streakStart();
        });
    }

    public boolean reminder() {
        return reminder;
    }

    public Instant reminderTimestamp() {
        return reminderTimestamp;
    }

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void reminderState(boolean state) {
        query("""
                UPDATE votes SET reminder = ? WHERE user_id = ? AND botlist = ?
                """)
                .single(call().bind(state).bind(userId).bind(botlist))
                .update()
                .ifChanged(i -> this.reminder = state);
    }

    public void reminderSent() {
        query("""
                UPDATE votes SET sent = TRUE WHERE user_id = ? AND botlist = ?
                """).single(call().bind(userId).bind(botlist)).update().ifChanged(i -> reminderSent = true);
    }

    public void snoozeReminder(Duration duration) {
        long seconds = duration.getSeconds();
        query("""
                UPDATE votes SET reminder_timestamp = now() + ?::INTERVAL WHERE user_id = ? AND botlist = ?
                """)
                .single(call().bind("%d SECONDS".formatted(seconds))
                        .bind(userId)
                        .bind(botlist))
                .update()
                .ifChanged(i -> reminderTimestamp = Instant.now().plus(duration));
    }
}
