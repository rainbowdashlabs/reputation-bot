package de.chojo.repbot.data;

import de.chojo.repbot.data.util.DbUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import javax.sql.DataSource;
import java.sql.SQLException;

public class RepData {
    private final DataSource source;

    public RepData(DataSource source) {
        this.source = source;
    }

    public boolean logReputation(Guild guild, User donor, User receiver, Message message) {
        try (var conn = source.getConnection()) {
            try (var stmt = conn.prepareStatement("""
                    INSERT INTO
                        reputation_log(guild_id, donor_id, receiver_id, message_id) VALUES(?,?,?,?)
                                        """)) {
                stmt.setLong(1, guild.getIdLong());
                stmt.setLong(2, donor.getIdLong());
                stmt.setLong(3, receiver.getIdLong());
                stmt.setLong(4, message.getIdLong());
                stmt.execute();
            }

            try (var stmt = conn.prepareStatement("""
                    INSERT INTO 
                        user_reputation (guild_id, user_id, reputation) VALUES (?,?,1)
                        ON CONFLICT(guild_id, user_id)
                            DO UPDATE
                                SET reputation = user_reputation.reputation + 1
                                        """)) {
                stmt.setLong(1, guild.getIdLong());
                stmt.setLong(2, receiver.getIdLong());
                stmt.execute();
                return true;
            }
        } catch (SQLException e) {
            DbUtil.logSQLError("Could not log reputation", e);
            return false;
        }
    }
}
