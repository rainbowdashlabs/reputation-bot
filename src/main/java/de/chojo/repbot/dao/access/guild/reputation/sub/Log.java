/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub;

import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.pagination.ReputationLogAccess;
import de.chojo.repbot.dao.snapshots.ReputationLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Log  implements GuildHolder {
    private final Reputation reputation;

    public Log(Reputation reputation) {
        this.reputation = reputation;
    }

    /**
     * Get the last log entries for reputation received by the user.
     *
     * @param user user
     * @return sorted list of entries. the most recent first.
     */
    public ReputationLogAccess getUserReceivedLog(User user, int pageSize) {
        return new ReputationLogAccess(() -> getUserReceivedLogPages(user, pageSize), page -> getUserReceivedLogPage(user, pageSize, page));
    }

    public ReputationLogAccess userDonatedLog(User user, int pageSize) {
        return new ReputationLogAccess(() -> getUserDonatedLogPages(user, pageSize), page -> getUserDonatedLogPage(user, pageSize, page));
    }

    /**
     * Get the last log entries for reputation received by the user.
     *
     * @param user user
     * @return sorted list of entries. the most recent first.
     */
    private List<ReputationLogEntry> getUserReceivedLogPage(User user, int pageSize, int page) {
        return getLog("receiver_id", user.getIdLong(), pageSize, page);
    }

    /**
     * Get the last log entries for reputation donated by the user.
     *
     * @param user user
     * @return sorted list of entries. the most recent first.
     */
    private List<ReputationLogEntry> getUserDonatedLogPage(User user, int pageSize, int page) {
        return getLog("donor_id", user.getIdLong(), pageSize, page);
    }

    /**
     * Get the log entries for a message
     *
     * @param messageId message id
     * @return sorted list of entries. the most recent first.
     */
    public List<ReputationLogEntry> messageLog(long messageId, int count) {
        return getLog("message_id", messageId, count, 0);
    }

    private List<ReputationLogEntry> getLog(String column, long id, int pageSize, int page) {
        return query("""
                       SELECT
                           guild_id,
                           donor_id,
                           receiver_id,
                           message_id,
                           received,
                           ref_message_id,
                           channel_id,
                           cause
                       FROM
                           reputation_log
                       WHERE
                           %s = ?
                           AND guild_id = ?
                       ORDER BY received DESC
                       OFFSET ?
                       LIMIT ?;
                       """, column)
                .single(call().bind(id).bind(guildId()).bind(page * pageSize).bind(pageSize))
                .map(r -> ReputationLogEntry.build(this, r))
                .all();
    }

    public Optional<ReputationLogEntry> getLatestReputation() {
        return query("""
                       SELECT
                           guild_id,
                           donor_id,
                           receiver_id,
                           message_id,
                           received,
                           ref_message_id,
                           channel_id,
                           cause
                       FROM reputation_log
                       WHERE guild_id = ?
                       ORDER BY received DESC
                       LIMIT 1;
                       """).single(call().bind(guildId()))
                .map(r -> ReputationLogEntry.build(this, r))
                .first();
    }

    /**
     * Get the first log entry for a message.
     *
     * @param message message id
     * @return a log entry if found
     */
    public List<ReputationLogEntry> getLogEntries(long message) {
        return query("""
                       SELECT
                           guild_id,
                           donor_id,
                           receiver_id,
                           message_id,
                           received,
                           ref_message_id,
                           channel_id,
                           cause
                       FROM
                           reputation_log
                       WHERE
                           message_id = ?
                           AND guild_id = ?;
                       """)
                .single(call().bind(message).bind(guildId()))
                .map(r -> ReputationLogEntry.build(this, r))
                .all();
    }

    /**
     * Get the log entries for a message.
     *
     * @param message message
     * @return a log entry if found
     */
    public List<ReputationLogEntry> getLogEntries(Message message) {
        return getLogEntries(message.getIdLong());
    }

    private int getUserDonatedLogPages(User user, int pageSize) {
        return getLogPages("donor_id", user.getIdLong(), pageSize);
    }

    private int getUserReceivedLogPages(User user, int pageSize) {
        return getLogPages("receiver_id", user.getIdLong(), pageSize);
    }

    private int getLogPages(String column, long id, int pageSize) {
        return query("""
                       SELECT
                           CEIL(COUNT(1)::numeric / ?) AS count
                       FROM
                           reputation_log
                       WHERE guild_id = ?
                           AND %s = ?;
                       """, column)
                .single(call().bind(pageSize).bind(guildId()).bind(id))
                .map(row -> row.getInt("count"))
                .first()
                .orElse(1);
    }

    @Override
    public Guild guild() {
        return reputation.guild();
    }

    @Override
    public long guildId() {
        return reputation.guildId();
    }
}
