package de.chojo.repbot.data;

import de.chojo.repbot.data.util.DbUtil;
import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.entities.Guild;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                    active_channels
                FROM
                    guild_settings
                WHERE 
                    guild_id = ?
                                """)) {
            stmt.setLong(1, guild.getIdLong());
            ResultSet rs = stmt.executeQuery();
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
                                DbUtil.arrayToArray(rs, "active_channels", new Long[0])
                        )
                );
            }
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not retrieve guild settings", e);
        }
        return Optional.empty();
    }

    public boolean initGuild(Guild guild) {
        try (var conn = source.getConnection();
             var stmt = conn.prepareStatement("INSERT INTO message_settings(guild_id) VALUES (?) ON CONFLICT DO NOTHING")) {
            stmt.setLong(1, guild.getIdLong());
            stmt.execute();
            return true;
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not init guild", e);
        }
        return false;
    }
}
