/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.gdpr;

import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import org.slf4j.Logger;

import java.sql.SQLException;

import static de.chojo.sadu.queries.api.call.Call.call;
import static org.slf4j.LoggerFactory.getLogger;

public final class RemovalTask {
    private static final Logger log = getLogger(RemovalTask.class);
    private final long taskId;
    private final long guildId;
    private final long userId;

    public RemovalTask(long taskId, long guildId, long userId) {
        this.taskId = taskId;
        this.guildId = guildId;
        this.userId = userId;
    }

    public static RemovalTask build(Row rs) throws SQLException {
        return new RemovalTask(rs.getLong("task_id"), rs.getLong("guild_id"), rs.getLong("user_id"));
    }

    public static void anonymExecute(long guildId, long userId) {
        new RemovalTask(-1L, guildId, userId).executeRemovalTask();
    }

    public void executeRemovalTask() {
        try (var conn = QueryConfiguration.getDefault().withSingleTransaction()) {
            if (userId() == 0) {
                conn.query("DELETE FROM guilds WHERE guild_id = ?;")
                        .single(call().bind(guildId))
                        .delete();
                log.trace("Removed guild settings for {}", guildId());
            } else if (guildId() == 0) {
                conn.query("DELETE FROM reputation_log WHERE receiver_id = ?;")
                        .single(call().bind(userId()))
                        .delete();
                conn.query("UPDATE reputation_log SET donor_id = NULL WHERE donor_id = ?;")
                        .single(call().bind(userId()))
                        .update();
                conn.query("DELETE FROM reputation_offset WHERE user_id = ?;")
                        .single(call().bind(userId()))
                        .delete();
                conn.query("DELETE FROM support_threads WHERE user_id = ?;")
                        .single(call().bind(userId()))
                        .delete();
                log.trace("Removed Data of user {}", userId());
            } else {
                // Remove user from guild
                conn.query("DELETE FROM reputation_log WHERE guild_id = ? AND receiver_id = ?;")
                        .single(call().bind(guildId()).bind(userId()))
                        .delete();
                conn.query("UPDATE reputation_log SET donor_id = NULL WHERE guild_id = ? AND donor_id = ?;")
                        .single(call().bind(guildId()).bind(userId()))
                        .delete();
                conn.query("DELETE FROM reputation_offset WHERE guild_id = ? AND user_id = ?;")
                        .single(call().bind(guildId()).bind(userId()))
                        .delete();
                log.trace("Removed user reputation from guild {} of user {}", guildId(), userId());
            }

            conn.query("DELETE FROM cleanup_schedule WHERE task_id = ?;")
                    .single(call().bind(taskId()))
                    .delete();
        }
    }

    public long taskId() {
        return taskId;
    }

    public long guildId() {
        return guildId;
    }

    public long userId() {
        return userId;
    }
}
