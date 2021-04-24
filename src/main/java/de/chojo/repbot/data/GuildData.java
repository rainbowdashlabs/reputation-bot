package de.chojo.repbot.data;

import de.chojo.repbot.data.util.DbUtil;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.data.wrapper.ReputationRole;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GuildData {
    private final DataSource source;

    public GuildData(DataSource source) {
        this.source = source;
    }

    public Optional<GuildSettings> getGuildSettings(Guild guild) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
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
                    cooldown
                FROM
                    guild_settings
                WHERE 
                    guild_id = ?
                                """)) {
            stmt.setLong(1, guild.getIdLong());
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(
                        new GuildSettings(guild,
                                rs.getString("prefix"),
                                DbUtil.arrayToArray(rs, "thankswords", new String[0]),
                                rs.getInt("max_message_age"),
                                rs.getString("reaction"),
                                rs.getBoolean("reactions_active"),
                                rs.getBoolean("answer_active"),
                                rs.getBoolean("mention_active"),
                                DbUtil.arrayToArray(rs, "active_channels", new Long[0]),
                                rs.getInt("cooldown"))
                );
            }
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not retrieve guild settings", e);
        }
        return Optional.empty();
    }

    public Optional<String> getPrefix(Guild guild) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                SELECT prefix FROM guild_prefix where guild_id = ?;
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

    public boolean setPrefix(Guild guild, @Nullable String prefix) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                INSERT INTO
                    guild_prefix(guild_id, prefix) VALUES (?,?)
                    ON CONFLICT(guild_id)
                        DO UPDATE
                            SET guild_prefix = excluded.prefix;
                """)) {
            stmt.setLong(1, guild.getIdLong());
            if (prefix == null) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, prefix);
            }
            return true;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not retrieve guild prefix", e);
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
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
                INSERT INTO guild_ranks(guild_id, role_id, reputation) VALUES(?,?,?)
                    ON CONFLICT(guild_id, role_id)
                        DO UPDATE
                            SET reputation = excluded.reputation
                """)) {
            stmt.setLong(1, guild.getIdLong());
            stmt.setLong(2, role.getIdLong());
            stmt.setLong(3, reputation);
            stmt.execute();
            return true;
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

    public boolean uppateMessageSettings(Guild guild, @Nullable Integer maxMessageAge, @Nullable String reaction,
                                         @Nullable Boolean reactionsActive, @Nullable Boolean answerActive, @Nullable Boolean mentionActive,
                                         @Nullable Boolean fuzzyActive, Integer cooldown) {
        try (var conn = source.getConnection(); var stmt = conn.prepareStatement("""
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
                """)) {
            if (maxMessageAge == null) {
                stmt.setNull(1, Types.INTEGER);
            } else {
                stmt.setInt(1, maxMessageAge);
            }
            if (reaction == null) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, reaction);
            }
            if (reactionsActive == null) {
                stmt.setNull(3, Types.BOOLEAN);
            } else {
                stmt.setBoolean(3, reactionsActive);
            }
            if (answerActive == null) {
                stmt.setNull(4, Types.BOOLEAN);
            } else {
                stmt.setBoolean(4, answerActive);
            }
            if (mentionActive == null) {
                stmt.setNull(5, Types.BOOLEAN);
            } else {
                stmt.setBoolean(5, mentionActive);
            }
            if (fuzzyActive == null) {
                stmt.setNull(6, Types.BOOLEAN);
            } else {
                stmt.setBoolean(6, fuzzyActive);
            }
            if (cooldown == null) {
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setInt(7, cooldown);
            }
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not update message settings", e);
        }
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
}
