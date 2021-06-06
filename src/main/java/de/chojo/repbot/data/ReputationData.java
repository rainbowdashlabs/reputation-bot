package de.chojo.repbot.data;

import de.chojo.jdautil.database.QueryObject;
import de.chojo.jdautil.database.builder.QueryBuilderConfig;
import de.chojo.jdautil.database.builder.QueryBuilderFactory;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.data.wrapper.ReputationLogEntry;
import de.chojo.repbot.data.wrapper.ReputationUser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;
public class ReputationData extends QueryObject {
    private final QueryBuilderFactory factory;
    private static final Logger log = getLogger(ReputationData.class);

    public ReputationData(DataSource source) {
        super(source);
        factory = new QueryBuilderFactory(QueryBuilderConfig.builder().build(), source);
    }

    public boolean logReputation(Guild guild, User donor, User receiver, Message message, @Nullable Message refMessage, ThankType type) {
        var success = factory.builder()
                .query("""
                        INSERT INTO
                        reputation_log(guild_id, donor_id, receiver_id, message_id, ref_message_id, channel_id, cause) VALUES(?,?,?,?,?,?,?)
                            ON CONFLICT(guild_id, donor_id, receiver_id, message_id)
                                DO NOTHING;
                        """)
                .paramsBuilder(b -> b.setLong(guild.getIdLong()).setLong(donor.getIdLong()).setLong(receiver.getIdLong())
                        .setLong(message.getIdLong()).setLong(refMessage ==  null ? null : refMessage.getIdLong())
                        .setLong(message.getChannel().getIdLong()).setString(type.name()))
                .insert().executeSync() > 0;
        if (success) {
            log.debug("{} received one reputation from {} for message {}", receiver.getName(), donor.getName(), message.getIdLong());
        }
        return success;
    }

    public Optional<Instant> getLastRated(Guild guild, User donor, User receiver) {
        return factory.builder(Instant.class).
                query("""
                        SELECT
                            received
                        FROM
                            reputation_log
                        WHERE
                            guild_id = ?
                            AND donor_id = ?
                            AND receiver_id = ?
                        ORDER BY received DESC
                        LIMIT  1;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(donor.getIdLong()).setLong(receiver.getIdLong()))
                .readRow(row -> row.getTimestamp("received").toInstant())
                .firstSync();
    }

    public List<ReputationUser> getRanking(Guild guild, int limit, int offset) {
        return factory.builder(ReputationUser.class)
                .query("""
                        SELECT
                            rank,
                            user_id,
                            reputation
                        from
                            user_reputation
                        WHERE guild_id = ?
                        ORDER BY reputation DESC
                        OFFSET ?
                        LIMIT ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setInt(offset).setInt(limit))
                .readRow(row -> new ReputationUser(row.getLong("rank"), row.getLong("user_id"), row.getLong("reputation")))
                .allSync();
    }

    public Optional<ReputationUser> getReputation(Guild guild, User user) {
        return factory.builder(ReputationUser.class)
                .query("""
                        SELECT rank, user_id, reputation from user_reputation where guild_id = ? and user_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(user.getIdLong()))
                .readRow(this::buildUser).firstSync();
    }

    private ReputationUser buildUser(ResultSet rs) throws SQLException {
        return new ReputationUser(
                rs.getLong("rank"),
                rs.getLong("user_id"),
                rs.getLong("reputation")
        );
    }

    public void removeMessage(long messageId) {
        factory.builder()
                .query("DELETE FROM reputation_log where message_id = ?;")
                .paramsBuilder(stmt -> stmt.setLong(messageId))
                .update().execute();
    }

    public Long getLastRatedDuration(Guild guild, User donor, User receiver, ChronoUnit unit) {
        return getLastRated(guild, donor, receiver).map(i -> i.until(Instant.now(), unit)).orElse(Long.MAX_VALUE);
    }

    public Optional<ReputationLogEntry> getLogEntry(Message message) {
        return factory.builder(ReputationLogEntry.class)
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
                        where
                            message_id = ?;
                        """)
                .params(stmt -> stmt.setLong(1, message.getIdLong()))
                .readRow(this::buildLogEntry).firstSync();
    }

    public List<ReputationLogEntry> getUserReceivedLog(User user, Guild guild, int count) {
        return factory.builder(ReputationLogEntry.class)
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
                        where
                            receiver_id = ?
                            AND guild_id = ?
                        ORDER BY received DESC
                        LIMIT ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(user.getIdLong()).setLong(guild.getIdLong()).setInt(count))
                .readRow(this::buildLogEntry).allSync();
    }

    public List<ReputationLogEntry> getUserDonatedLog(User user, Guild guild, int count) {
        return factory.builder(ReputationLogEntry.class)
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
                        where
                            donor_id = ?
                            AND guild_id = ?
                        ORDER BY received DESC
                        LIMIT ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(user.getIdLong()).setLong(guild.getIdLong()).setInt(count))
                .readRow(this::buildLogEntry).allSync();
    }

    public List<ReputationLogEntry> getMessageLog(long messageId, Guild guild, int count) {
        return factory.builder(ReputationLogEntry.class)
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
                        where
                            message_id = ?
                            AND guild_id = ?
                        ORDER BY received DESC
                        LIMIT ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(messageId).setLong(guild.getIdLong()).setInt(count))
                .readRow(this::buildLogEntry)
                .allSync();
    }

    private ReputationLogEntry buildLogEntry(ResultSet rs) throws SQLException {
        return new ReputationLogEntry(
                rs.getLong("guild_id"),
                rs.getLong("channel_id"),
                rs.getLong("donor_id"),
                rs.getLong("receiver_id"),
                rs.getLong("message_id"),
                rs.getLong("ref_message_id"),
                ThankType.valueOf(rs.getString("cause"))
        );
    }

    public boolean removeReputation(long user, long message, ThankType type) {
        return factory.builder()
                .query("""
                        DELETE FROM
                            reputation_log
                        where
                            message_id = ?
                            AND donor_id = ?
                            AND cause = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(message).setLong(user).setString(type.name()))
                .update()
                .executeSync() > 0;
    }
}
