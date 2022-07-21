package de.chojo.repbot.dao.access.gdpr;

import de.chojo.repbot.dao.access.Gdpr;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.stage.ResultStage;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.slf4j.LoggerFactory.getLogger;

public final class RemovalTask extends QueryFactoryHolder {
    private static final Logger log = getLogger(RemovalTask.class);
    private final long taskId;
    private final long guildId;
    private final long userId;

    public RemovalTask(QueryFactoryHolder holder, long taskId, long guildId, long userId) {
        super(holder);
        this.taskId = taskId;
        this.guildId = guildId;
        this.userId = userId;
    }

    public static RemovalTask build(Gdpr gdpr, ResultSet rs) throws SQLException {
        return new RemovalTask(gdpr, rs.getLong("task_id"), rs.getLong("guild_id"), rs.getLong("user_id"));
    }

    public static void anonymExecute(QueryFactoryHolder holder, long guildId, long userId) {
        new RemovalTask(holder, -1L, guildId, userId).executeRemovalTask();
    }

    public void executeRemovalTask() {
        ResultStage<Void> builder;
        if (userId() == 0) {
            // Remove guild
            builder = builder().query("DELETE FROM reputation_log WHERE guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(guildId()))
                    .append().query("DELETE FROM guild_settings WHERE guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(guildId()))
                    .append().query("DELETE FROM active_channel WHERE guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(guildId()))
                    .append().query("DELETE FROM reputation_settings WHERE guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(guildId()))
                    .append().query("DELETE FROM guild_ranks WHERE guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(guildId()))
                    .append().query("DELETE FROM thankwords WHERE guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(guildId()));
            log.trace("Removed guild settings for {}", guildId());
        } else if (guildId() == 0) {
            // Remove complete user
            builder = builder().query("DELETE FROM reputation_log WHERE receiver_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(userId()))
                    .append().query("UPDATE reputation_log SET donor_id = NULL WHERE donor_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(userId()))
                    .append().query("DELETE FROM reputation_offset WHERE user_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(userId()));
            log.trace("Removed Data of user {}", userId());
        } else {
            // Remove user from guild
            builder = builder().query("DELETE FROM reputation_log WHERE guild_id = ? AND receiver_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(userId()))
                    .append().query("UPDATE reputation_log SET donor_id = NULL WHERE guild_id = ? AND donor_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(userId()))
                    .append().query("DELETE FROM reputation_offset WHERE guild_id = ? AND user_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(userId()));
            log.trace("Removed user reputation from guild {} of user {}", guildId(), userId());
        }

        builder.append().query("DELETE FROM cleanup_schedule WHERE task_id = ?;")
                .params(stmt -> stmt.setLong(1, taskId()))
                .update().executeSync();
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
