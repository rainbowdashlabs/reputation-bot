package de.chojo.repbot.data;

import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.data.util.DbUtil;
import de.chojo.repbot.data.wrapper.ReputationLogEntry;
import de.chojo.repbot.data.wrapper.ReputationUser;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ReputationData {
    private final DataSource source;

    public ReputationData(DataSource source) {
        this.source = source;
    }

    public boolean logReputation(Guild guild, User donor, User receiver, Message message, @Nullable Message refMessage, ThankType type) {
        try (var conn = source.getConnection()) {
            try (var stmt = conn.prepareStatement("""
                    INSERT INTO
                        reputation_log(guild_id, donor_id, receiver_id, message_id, ref_message_id, channel_id, cause) VALUES(?,?,?,?,?,?,?)
                            ON CONFLICT(guild_id, donor_id, receiver_id, message_id)
                                DO NOTHING;
                                        """)) {
                stmt.setLong(1, guild.getIdLong());
                stmt.setLong(2, donor.getIdLong());
                stmt.setLong(3, receiver.getIdLong());
                stmt.setLong(4, message.getIdLong());
                if (refMessage == null) {
                    stmt.setNull(5, Types.BIGINT);
                } else {
                    stmt.setLong(5, refMessage.getIdLong());
                }
                stmt.setLong(6, message.getChannel().getIdLong());
                stmt.setString(7, type.name());
                log.debug("{} received one reputation from {}", receiver.getName(), donor.getName());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not log reputation", e);
            return false;
        }
    }

    public Optional<Instant> getLastRated(Guild guild, User donor, User receiver) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
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
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setLong(2, donor.getIdLong());
            stmt.setLong(3, receiver.getIdLong());
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.ofNullable(rs.getTimestamp("received").toInstant());
            }
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not get last rated.", e);
        }
        return Optional.empty();
    }

    public List<ReputationUser> getRanking(Guild guild, int limit, int offset) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                SELECT
                    rank,
                    user_id,
                    reputation
                from
                    user_reputation
                WHERE guild_id = ?
                ORDER BY reputation DESC
                OFFSET ?
                LIMIT ?
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setLong(2, offset);
            stmt.setLong(3, limit);
            var rs = stmt.executeQuery();
            var users = new ArrayList<ReputationUser>();
            while (rs.next()) {
                users.add(
                        new ReputationUser(
                                rs.getLong("rank"),
                                rs.getLong("user_id"),
                                rs.getLong("reputation")
                        )
                );
            }
            return users;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not retrieve user ranking", e);
        }
        return Collections.emptyList();
    }

    public Optional<ReputationUser> getReputation(Guild guild, User user) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                SELECT rank, user_id, reputation from user_reputation where guild_id = ? and user_id = ?
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setLong(2, user.getIdLong());
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(buildUser(rs));
            }
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not retrieve user reputation", e);
        }
        return Optional.empty();
    }

    private ReputationUser buildUser(ResultSet rs) throws SQLException {
        return new ReputationUser(
                rs.getLong("rank"),
                rs.getLong("user_id"),
                rs.getLong("reputation")
        );
    }

    public void removeMessage(long messageId) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                DELETE FROM reputation_log where message_id = ?;
                """)) {
            stmt.setLong(1, messageId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not delete message from log", e);
        }
    }

    public Long getLastRatedDuration(Guild guild, User donor, User receiver, ChronoUnit unit) {
        return getLastRated(guild, donor, receiver).map(i -> i.until(Instant.now(), unit)).orElse(Long.MAX_VALUE);
    }

    public Optional<ReputationLogEntry> getLogEntry(Message message) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
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
                """)) {
            stmt.setLong(1, message.getIdLong());
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(buildLogEntry(rs));
            }
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not retrieve log entry", e);
        }
        return Optional.empty();
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
}
