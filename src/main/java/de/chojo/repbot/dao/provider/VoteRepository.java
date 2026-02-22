/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.provider;

import de.chojo.repbot.dao.access.vote.VoteLog;
import de.chojo.repbot.dao.access.vote.VoteReason;
import de.chojo.repbot.dao.access.vote.VoteStreak;

import java.time.Instant;
import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class VoteRepository {

    public List<VoteLog> getVoteLog(long userId, int page, int entries) {
        return query("SELECT * FROM vote_log WHERE user_id = ? ORDER BY created DESC LIMIT ? OFFSET ?")
                .single(call().bind(userId).bind(entries).bind(page * entries))
                .map(VoteLog::map)
                .all();
    }

    public long getVoteLogPages(long userId, int entries) {
        return query("SELECT count(1) FROM vote_log WHERE user_id = ?")
                .single(call().bind(userId))
                .mapAs(Long.class)
                .first()
                .map(e -> (long) Math.ceil((double) e / entries))
                .orElse(0L);
    }

    public VoteStreak getLastVote(long userId, String botlist) {
        return query("""
                        SELECT * FROM votes WHERE user_id = ? AND botlist = ?;
                """)
                .single(call().bind(userId).bind(botlist))
                .mapAs(VoteStreak.class)
                .first()
                .orElseGet(() -> new VoteStreak(userId, botlist, Instant.EPOCH, 0));
    }

    public void addToken(long userId, long guildId, int amount, VoteReason reason) {
        logToken(userId, guildId, reason, amount);
        if (guildId == 0) {
            addToken(userId, "USER", amount);
        }
        addToken(guildId, "GUILD", amount);
    }

    public int getVoteCountToday(long userId) {
        return query("""
                SELECT count(*) FROM votes WHERE user_id = ? AND last_vote::DATE = now()::DATE;
                """)
                .single(call().bind(userId))
                .mapAs(Integer.class)
                .first()
                .orElse(0);
    }

    private void addToken(long userId, String type, int amount) {
        query("""
                INSERT
                INTO
                    vote_token(entity_id, entity_type, token, total_token)
                VALUES
                    (?, ?, ?, ?)
                ON CONFLICT(entity_id, entity_type) DO UPDATE SET
                    token = token + excluded.token, total_token = total_token + excluded.token;
                """).single(call().bind(userId).bind(type).bind(amount).bind(amount)).insert();
    }

    private void logToken(long userId, long guildId, VoteReason reason, int amount) {
        query("""
                INSERT INTO vote_log(user_id, guild_id, tokens, reason) VALUES (?,?,?,?);""")
                .single(call().bind(userId).bind(guildId).bind(amount).bind(reason))
                .insert();
    }

    public int getUserToken(long userId) {
        return query("SELECT token FROM vote_token WHERE entity_id = ? AND entity_type = 'USER'")
                .single(call().bind(userId))
                .mapAs(Integer.class)
                .first()
                .orElse(0);
    }

    public long getGuildToken(long guildId) {
        return query("SELECT token FROM vote_token WHERE entity_id = ? AND entity_type = 'GUILD'")
                .single(call().bind(guildId))
                .mapAs(Long.class)
                .first()
                .orElse(0L);
    }

    public boolean withdrawUserTokens(long id, int amount) {
        return withdrawTokens(id, "USER", amount);
    }

    public boolean withdrawGuildTokens(long id, int amount) {
        return withdrawTokens(id, "GUILD", amount);
    }

    public boolean transferToGuild(long userId, long guildId, int amount) {
        boolean success = withdrawUserTokens(userId, amount);
        if (success) logToken(userId, guildId, VoteReason.TRANSFER, amount);
        return success;
    }

    private boolean withdrawTokens(long id, String entityType, int amount) {
        return query("""
                UPDATE vote_token
                SET
                    token = token - :token
                WHERE token >= :token
                  AND entity_type = :entity_type
                  AND entity_id = :id""")
                .single(call().bind("token", amount)
                        .bind("entity_type", entityType)
                        .bind("id", id))
                .update()
                .changed();
    }
}
