package de.chojo.repbot.data;

import de.chojo.jdautil.database.QueryObject;
import de.chojo.jdautil.database.builder.QueryBuilderConfig;
import de.chojo.jdautil.database.builder.QueryBuilderFactory;
import de.chojo.jdautil.database.builder.stage.ResultStage;
import de.chojo.jdautil.localization.util.Language;
import de.chojo.repbot.data.util.DbUtil;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.data.wrapper.RemovalTask;
import de.chojo.repbot.data.wrapper.ReputationRole;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class GuildData extends QueryObject {
    private final DataSource source;
    private final QueryBuilderFactory factory;

    public GuildData(DataSource source) {
        super(source);
        this.source = source;
        factory = new QueryBuilderFactory(QueryBuilderConfig.builder().build(), source);
    }

    public Optional<GuildSettings> getGuildSettings(Guild guild) {
        return factory.builder(GuildSettings.class)
                .query("""
                        SELECT
                            prefix,
                            thankswords,
                            max_message_age,
                            reaction,
                            reactions_active,
                            answer_active,
                            mention_active,
                            fuzzy_active,
                            active_channels,
                            cooldown,
                            manager_role
                        FROM
                            guild_settings
                        WHERE
                            guild_id = ?

                        """)
                .params(stmt -> stmt.setLong(1, guild.getIdLong()))
                .readRow(row -> new GuildSettings(guild,
                        row.getString("prefix"),
                        DbUtil.arrayToArray(row, "thankswords", new String[0]),
                        row.getInt("max_message_age"),
                        row.getString("reaction"),
                        row.getBoolean("reactions_active"),
                        row.getBoolean("answer_active"),
                        row.getBoolean("mention_active"),
                        row.getBoolean("fuzzy_active"),
                        DbUtil.arrayToArray(row, "active_channels", new Long[0]),
                        row.getInt("cooldown"),
                        row.getLong("manager_role")))
                .firstSync();
    }

    public Optional<String> getPrefix(Guild guild) {
        return factory.builder(String.class).query("SELECT prefix FROM guild_bot_settings where guild_id = ?;")
                .params(stmt -> stmt.setLong(1, guild.getIdLong()))
                .readRow(row -> row.getString(1))
                .firstSync();
    }

    public boolean setPrefix(Guild guild, @Nullable String prefix) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                INSERT INTO
                    guild_bot_settings(guild_id, prefix) VALUES (?,?)
                    ON CONFLICT(guild_id)
                        DO UPDATE
                            SET prefix = excluded.prefix;
                """)) {
            stmt.setLong(1, guild.getIdLong());
            if (prefix == null) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, prefix);
            }
            stmt.execute();
            return true;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not retrieve guild prefix", e);
        }
        return false;
    }

    public Optional<String> getLanguage(Guild guild) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                SELECT
                    language
                FROM
                    guild_bot_settings
                where guild_id = ?;
                """)) {
            stmt.setLong(1, guild.getIdLong());
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.ofNullable(rs.getString(1));
            }
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not retrieve guild prefix", e);
        }
        return Optional.empty();
    }

    public boolean setLanguage(Guild guild, @Nullable Language language) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                INSERT INTO
                    guild_bot_settings(guild_id, language) VALUES (?,?)
                    ON CONFLICT(guild_id)
                        DO UPDATE
                            SET language = excluded.language;
                """)) {
            stmt.setLong(1, guild.getIdLong());
            if (language == null) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, language.getCode());
            }
            stmt.execute();
            return true;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not retrieve guild language", e);
        }
        return false;
    }

    public boolean addChannel(Guild guild, MessageChannel channel) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                INSERT INTO active_channel(guild_id, channel_id) VALUES(?,?) ON CONFLICT DO NOTHING;
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setLong(2, channel.getIdLong());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not add channel", e);
        }
        return false;
    }

    public boolean deleteChannel(Guild guild, MessageChannel channel) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                DELETE FROM active_channel where guild_id = ? and channel_id = ?;
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setLong(2, channel.getIdLong());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not add channel", e);
        }
        return false;
    }

    public int clearChannel(Guild guild) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                DELETE FROM active_channel where guild_id = ?;
                """)) {
            stmt.setLong(1, guild.getIdLong());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not add channel", e);
        }
        return 0;
    }

    public boolean addReputationRole(Guild guild, Role role, long reputation) {
        try (var conn = source.getConnection()) {
            try (var stmt = conn.prepareStatement("""
                    DELETE FROM
                        guild_ranks
                    WHERE
                        guild_id = ?
                            AND (role_id = ?
                                OR reputation = ?)
                    """)) {
                stmt.setLong(1, guild.getIdLong());
                stmt.setLong(2, role.getIdLong());
                stmt.setLong(3, reputation);
                stmt.executeUpdate();
            }

            try (var stmt = conn.prepareStatement("""
                    INSERT INTO guild_ranks(guild_id, role_id, reputation) VALUES(?,?,?)
                        ON CONFLICT(guild_id, role_id)
                            DO UPDATE
                                SET reputation = excluded.reputation,
                                    role_id = excluded.role_id;
                    """)) {
                stmt.setLong(1, guild.getIdLong());
                stmt.setLong(2, role.getIdLong());
                stmt.setLong(3, reputation);
                stmt.execute();
                return true;
            }
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not add reputation role.", e);
        }
        return false;
    }

    public boolean removeReputationRole(Guild guild, Role role) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                DELETE FROM guild_ranks where guild_id =? and role_id = ?
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setLong(2, role.getIdLong());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not remove reputation role.", e);
        }
        return false;
    }

    public Optional<ReputationRole> getCurrentReputationRole(Guild guild, long reputation) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                SELECT
                    role_id,
                    reputation
                from
                    guild_ranks
                where guild_id = ?
                    AND reputation <= ?
                order by reputation DESC
                limit 1
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setLong(2, reputation);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(buildRole(rs));
            }
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not add reputation role.", e);
        }
        return Optional.empty();
    }

    public Optional<ReputationRole> getNextReputationRole(Guild guild, long reputation) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                SELECT
                    role_id,
                    reputation
                from
                    guild_ranks
                where guild_id = ?
                    AND reputation > ?
                order by reputation
                limit 1
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setLong(2, reputation);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(buildRole(rs));
            }
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not add reputation role.", e);
        }
        return Optional.empty();
    }

    public List<ReputationRole> getReputationRoles(Guild guild) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                SELECT
                    role_id,
                    reputation
                from
                    guild_ranks
                where guild_id = ?
                order by reputation
                """)) {
            stmt.setLong(1, guild.getIdLong());
            var rs = stmt.executeQuery();
            List<ReputationRole> roles = new ArrayList<>();
            while (rs.next()) {
                roles.add(buildRole(rs));
            }
            return roles;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not add reputation role.", e);
        }
        return Collections.emptyList();
    }

    public boolean updateMessageSettings(Guild guild, @Nullable Integer maxMessageAge, @Nullable String reaction,
                                         @Nullable Boolean reactionsActive, @Nullable Boolean answerActive, @Nullable Boolean mentionActive,
                                         @Nullable Boolean fuzzyActive, Integer cooldown) {
        return factory.builder()
                .query("""
                        UPDATE
                            message_settings
                        SET max_message_age = coalesce(?, max_message_age),
                            reaction = coalesce(?, reaction),
                            reactions_active = coalesce(?, reactions_active),
                            answer_active = coalesce(?, answer_active),
                            mention_active = coalesce(?, mention_active),
                            fuzzy_active = coalesce(?, fuzzy_active),
                            cooldown = coalesce(?, cooldown)
                        where guild_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setInt(maxMessageAge).setString(reaction).setBoolean(reactionsActive)
                .setBoolean(answerActive).setBoolean(mentionActive).setBoolean(fuzzyActive).setInt(cooldown)
                .setLong(guild.getIdLong()))
                .update().executeSync() > 0;
    }

    public boolean addThankWord(Guild guild, String pattern) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                INSERT INTO
                    thankwords(guild_id, thankword) VALUES(?,?)
                        ON CONFLICT
                            DO NOTHING;
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setString(2, pattern);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not add thankword", e);
        }
        return false;
    }

    public boolean removeThankWord(Guild guild, String pattern) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                DELETE FROM
                    thankwords
                WHERE
                    guild_id = ?
                    and thankword = ?
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setString(2, pattern);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not add thankword", e);
        }
        return false;
    }

    private ReputationRole buildRole(ResultSet rs) throws SQLException {
        return new ReputationRole(
                rs.getLong("role_id"),
                rs.getLong("reputation")
        );
    }

    public void initGuild(Guild guild) {
        try (var conn = source.getConnection();
             var stmt = conn.prepareStatement("INSERT INTO message_settings(guild_id) VALUES (?) ON CONFLICT DO NOTHING")) {
            stmt.setLong(1, guild.getIdLong());
            stmt.execute();
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not init guild", e);
        }
    }

    public boolean setManagerRole(Guild guild, Role role) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                INSERT INTO
                    guild_bot_settings(guild_id, manager_role) VALUES (?,?)
                    ON CONFLICT(guild_id)
                        DO UPDATE
                            SET manager_role = excluded.manager_role;
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setLong(2, role.getIdLong());
            stmt.execute();
            return true;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not set guild manager role", e);
        }
        return false;
    }

    public void queueDeletion(Guild guild) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                INSERT INTO
                    cleanup_schedule(guild_id)
                    VALUES (?)
                        ON CONFLICT(guild_id, user_id)
                            DO NOTHING;
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.executeUpdate();
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not queue guild deletion", e);
        }
    }

    public void queueDeletion(User user, Guild guild) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                INSERT INTO
                    cleanup_schedule(guild_id, user_id)
                    VALUES (?,?)
                        ON CONFLICT(guild_id, user_id)
                            DO NOTHING;
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setLong(2, user.getIdLong());
            stmt.executeUpdate();
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not queue member deletion", e);
        }
    }

    public void dequeueDeletion(Guild guild) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                DELETE FROM
                    cleanup_schedule
                where guild_id = ?
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.executeUpdate();
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not dequeue guild deletion", e);
        }
    }

    public void dequeueDeletion(Member member) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                DELETE FROM
                    cleanup_schedule
                where guild_id = ?
                    AND user_id = ?
                """)) {
            stmt.setLong(1, member.getGuild().getIdLong());
            stmt.setLong(2, member.getIdLong());
            stmt.executeUpdate();
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not dequeue member deletion", e);
        }
    }

    public List<RemovalTask> getRemovalTasks() {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                SELECT
                    task_id,
                    user_id,
                    guild_id
                from
                    cleanup_schedule
                where delete_after < now();
                """)) {
            var rs = stmt.executeQuery();
            var results = new ArrayList<RemovalTask>();
            while (rs.next()) {
                results.add(new RemovalTask(
                        rs.getLong("task_id"),
                        rs.getLong("guild_id"),
                        rs.getLong("user_id")
                ));
            }
            return results;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not retrieve removal tasks", e);
        }
        return Collections.emptyList();
    }

    public void executeRemovalTask(RemovalTask task) {
        ResultStage<Void> builder;
        if (task.getUserId() == null) {
            builder = factory.builder().query("DELETE FROM reputation_log where guild_id = ?;")
                    .params(stmt -> stmt.setLong(1, task.getGuildId()))
                    .append().query("DELETE FROM guild_bot_settings where guild_id = ?;")
                    .params(stmt -> stmt.setLong(1, task.getGuildId()))
                    .append().query("DELETE FROM active_channel where guild_id = ?;")
                    .params(stmt -> stmt.setLong(1, task.getGuildId()))
                    .append().query("DELETE FROM message_settings where guild_id = ?;")
                    .params(stmt -> stmt.setLong(1, task.getGuildId()))
                    .append().query("DELETE FROM guild_ranks where guild_id = ?;")
                    .params(stmt -> stmt.setLong(1, task.getGuildId()))
                    .append().query("DELETE FROM thankwords where guild_id = ?;")
                    .params(stmt -> stmt.setLong(1, task.getGuildId()));
            log.info("Removed guild settings for {}", task.getGuildId());
        } else {
            builder = factory.builder().query("DELETE FROM reputation_log where guild_id = ? AND receiver_id = ?;")
                    .params(stmt -> {
                        stmt.setLong(1, task.getGuildId());
                        stmt.setLong(2, task.getUserId());
                    })
                    .append().query("UPDATE reputation_log SET donor_id = 0 where guild_id = ? AND donor_id = ?;")
                    .params(stmt -> {
                        stmt.setLong(1, task.getGuildId());
                        stmt.setLong(2, task.getUserId());
                    });
            log.info("Removed user reputation from guild {} of user {}", task.getGuildId(), task.getUserId());
        }

        builder.append().query("DELETE FROM cleanup_schedule where task_id = ?;")
                .params(stmt -> stmt.setLong(1, task.getTaskId()))
                .update().executeSync();
    }
}
