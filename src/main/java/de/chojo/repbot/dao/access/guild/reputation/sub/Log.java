package de.chojo.repbot.dao.access.guild.reputation.sub;

import de.chojo.repbot.dao.access.reputation.Reputation;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.snapshots.ReputationLogEntry;
import de.chojo.repbot.data.wrapper.ReputationLogAccess;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Optional;

public class Log extends QueryFactoryHolder implements GuildHolder {
    private final Reputation reputation;

    public Log(Reputation reputation) {
        super(reputation);
        this.reputation = reputation;
    }

    /**
     * Get the last log entries for reputation received by the user.
     *
     * @param user  user
     * @return sorted list of entries. the most recent first.
     */
    public ReputationLogAccess getUserReceivedLog(User user, int pageSize) {
        return new ReputationLogAccess(() -> getUserReceivedLogPages(user, pageSize), page -> getUserReceivedLogPage(user,  pageSize, page));
    }

    public ReputationLogAccess getUserDonatedLog(User user, int pageSize) {
        return new ReputationLogAccess(() -> getUserDonatedLogPages(user, pageSize), page -> getUserDonatedLogPage(user,  pageSize, page));
    }

    /**
     * Get the last log entries for reputation received by the user.
     *
     * @param user  user
     * @return sorted list of entries. the most recent first.
     */
    private List<ReputationLogEntry> getUserReceivedLogPage(User user, int pageSize, int page) {
        return getLog( "receiver_id", user.getIdLong(), pageSize, page);
    }

    /**
     * Get the last log entries for reputation donated by the user.
     *
     * @param user  user
     * @return sorted list of entries. the most recent first.
     */
    private List<ReputationLogEntry> getUserDonatedLogPage(User user, int pageSize, int page) {
        return getLog( "donor_id", user.getIdLong(), pageSize, page);
    }

    /**
     * Get the log entried for a message
     *
     * @param messageId message id
     * @return sorted list of entries. the most recent first.
     */
    public List<ReputationLogEntry> getMessageLog(long messageId, int count) {
        return getLog( "message_id", messageId, count, 0);
    }

    public List<ReputationLogEntry> getLog(String column, long id, int pageSize, int page) {
        return builder(ReputationLogEntry.class)
                .query("""
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
                .paramsBuilder(stmt -> stmt.setLong(id).setLong(guildId()).setInt(page * pageSize).setInt(pageSize))
                .readRow(r -> ReputationLogEntry.build(this, r))
                .allSync();
    }

    public Optional<ReputationLogEntry> getLatestReputation() {
        return builder(ReputationLogEntry.class)
                .query("""
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
                        """).paramsBuilder(stmt -> stmt.setLong(guildId()))
                .readRow(r -> ReputationLogEntry.build(this, r))
                .firstSync();
    }

    /**
     * Get the log entries for a message.
     *
     * @param message message
     * @return a log entry if found
     */
    public Optional<ReputationLogEntry> getLogEntry(Message message) {
        return builder(ReputationLogEntry.class)
                .query("""
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
                .paramsBuilder(stmt -> stmt.setLong(message.getIdLong()).setLong(guildId()))
                .readRow(r -> ReputationLogEntry.build(this, r)).firstSync();
    }

    private int getUserDonatedLogPages(User user, int pageSize) {
        return getLogPages( "donor_id", user.getIdLong(), pageSize);
    }

    private int getUserReceivedLogPages(User user, int pageSize) {
        return getLogPages("receiver_id", user.getIdLong(), pageSize);
    }

    private int getLogPages(String column, long id, int pageSize) {
        return builder(Integer.class)
                .query("""
                        SELECT
                            CEIL(COUNT(1)::numeric / ?) AS count
                        FROM
                            reputation_log
                        WHERE guild_id = ?
                            AND %s = ?;
                        """, column)
                .paramsBuilder(stmt -> stmt.setInt(pageSize).setLong(guildId()).setLong(id))
                .readRow(row -> row.getInt("count"))
                .firstSync()
                .orElse(1);
    }

    @Override
    public Guild guild() {
        return reputation.guild();
    }
}
