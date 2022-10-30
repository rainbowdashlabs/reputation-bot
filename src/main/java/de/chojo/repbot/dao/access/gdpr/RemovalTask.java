package de.chojo.repbot.dao.access.gdpr;

import de.chojo.repbot.dao.access.Gdpr;
import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.stage.ResultStage;
import de.chojo.sadu.wrapper.util.Row;
import org.slf4j.Logger;

import java.sql.SQLException;

import static org.slf4j.LoggerFactory.getLogger;

public final class RemovalTask extends QueryFactory {
    private static final Logger log = getLogger(RemovalTask.class);
    private final long taskId;
    private final long guildId;
    private final long userId;

    public RemovalTask(QueryFactory holder, long taskId, long guildId, long userId) {
        super(holder);
        this.taskId = taskId;
        this.guildId = guildId;
        this.userId = userId;
    }

    public static RemovalTask build(Gdpr gdpr, Row rs) throws SQLException {
        return new RemovalTask(gdpr, rs.getLong("task_id"), rs.getLong("guild_id"), rs.getLong("user_id"));
    }

    public static void anonymExecute(QueryFactory holder, long guildId, long userId) {
        new RemovalTask(holder, -1L, guildId, userId).executeRemovalTask();
    }

    public void executeRemovalTask() {
        ResultStage<Void> builder;
        if (userId() == 0) {
            // Remove guild
            builder = builder().query("DELETE FROM reputation_log WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM guild_settings WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM active_channel WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM abuse_protection WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM active_categories WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM reputation_settings WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM guild_ranks WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM thankwords WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM thank_settings WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM reputation_offset WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM receiver_roles WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM donor_roles WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM guild_reactions WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()))
                               .append().query("DELETE FROM announcements WHERE guild_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()));
            log.trace("Removed guild settings for {}", guildId());
        } else if (guildId() == 0) {
            // Remove complete user
            builder = builder().query("DELETE FROM reputation_log WHERE receiver_id = ?;")
                               .parameter(stmt -> stmt.setLong(userId()))
                               .append().query("UPDATE reputation_log SET donor_id = NULL WHERE donor_id = ?;")
                               .parameter(stmt -> stmt.setLong(userId()))
                               .append().query("DELETE FROM reputation_offset WHERE user_id = ?;")
                               .parameter(stmt -> stmt.setLong(userId()));
            log.trace("Removed Data of user {}", userId());
        } else {
            // Remove user from guild
            builder = builder().query("DELETE FROM reputation_log WHERE guild_id = ? AND receiver_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()).setLong(userId()))
                               .append()
                               .query("UPDATE reputation_log SET donor_id = NULL WHERE guild_id = ? AND donor_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()).setLong(userId()))
                               .append().query("DELETE FROM reputation_offset WHERE guild_id = ? AND user_id = ?;")
                               .parameter(stmt -> stmt.setLong(guildId()).setLong(userId()));
            log.trace("Removed user reputation from guild {} of user {}", guildId(), userId());
        }

        builder.append().query("DELETE FROM cleanup_schedule WHERE task_id = ?;")
               .parameter(stmt -> stmt.setLong(taskId()))
               .update()
               .sendSync();
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
