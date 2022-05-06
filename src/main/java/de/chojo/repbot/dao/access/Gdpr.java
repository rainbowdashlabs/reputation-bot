package de.chojo.repbot.dao.access;

import de.chojo.repbot.data.wrapper.RemovalTask;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.stage.ResultStage;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class Gdpr extends QueryFactoryHolder {
    private static final Logger log = getLogger(Gdpr.class);

    public Gdpr(DataSource dataSource) {
        super(dataSource);
    }

    public boolean queueUserDeletion(User user) {
        return builder()
                       .query("""
                               INSERT INTO
                                   cleanup_schedule(user_id, delete_after)
                                   VALUES (?, NOW())
                                       ON CONFLICT(guild_id, user_id)
                                           DO NOTHING;
                               """)
                       .paramsBuilder(stmt -> stmt.setLong(user.getIdLong()))
                       .update()
                       .executeSync() > 0;
    }

    public List<RemovalTask> getRemovalTasks() {
        return builder(RemovalTask.class)
                .queryWithoutParams("""
                        SELECT
                            task_id,
                            user_id,
                            guild_id
                        FROM
                            cleanup_schedule
                        WHERE delete_after < NOW();
                        """)
                .readRow(rs -> new RemovalTask(rs.getLong("task_id"), rs.getLong("guild_id"), rs.getLong("user_id")))
                .allSync();
    }

    public void cleanupRequests() {
        builder()
                .queryWithoutParams("""
                        DELETE FROM gdpr_log
                        WHERE received IS NOT NULL
                            AND received < NOW() - INTERVAL '90 DAYS';
                        """)
                .update()
                .executeSync();
    }

    public boolean request(User user) {
        return builder()
                       .query("""
                               DELETE FROM gdpr_log
                               WHERE user_id = ?
                                   AND received IS NOT NULL
                                   AND received < NOW() - INTERVAL '30 days';
                               """)
                       .paramsBuilder(stmt -> stmt.setLong(user.getIdLong()))
                       .append()
                       .query("""
                               INSERT INTO gdpr_log(user_id) VALUES(?)
                                   ON CONFLICT(user_id)
                                       DO NOTHING;
                               """)
                       .paramsBuilder(stmt -> stmt.setLong(user.getIdLong()))
                       .update()
                       .executeSync() > 0;
    }

    public Optional<String> getUserData(User user) {
        return builder(String.class)
                .query("SELECT aggregate_user_data(?)")
                .paramsBuilder(stmt -> stmt.setLong(user.getIdLong()))
                .readRow(rs -> rs.getString(1))
                .firstSync();
    }

    public List<Long> getReportRequests() {
        return builder(Long.class)
                .queryWithoutParams("SELECT user_id FROM gdpr_log WHERE received IS NULL")
                .readRow(rs -> rs.getLong(1))
                .allSync();
    }

    public void markAsSend(Long user) {
        builder()
                .query("UPDATE gdpr_log SET received = NOW() WHERE user_id = ?")
                .paramsBuilder(stmt -> stmt.setLong(user))
                .update()
                .executeSync();
    }

    public void markAsFailed(long user) {
        builder()
                .query("UPDATE gdpr_log SET attempts = attempts + 1 WHERE user_id = ?")
                .paramsBuilder(stmt -> stmt.setLong(user))
                .update()
                .executeSync();
    }

    public void executeRemovalTask(RemovalTask task) {
        ResultStage<Void> builder;
        if (task.userId() == 0) {
            builder = builder().query("DELETE FROM reputation_log WHERE guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM guild_settings WHERE guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM active_channel WHERE guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM message_settings WHERE guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM guild_ranks WHERE guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM thankwords WHERE guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()));
            log.trace("Removed guild settings for {}", task.guildId());
        } else if (task.guildId() == 0) {
            builder = builder().query("DELETE FROM reputation_log WHERE receiver_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.userId()))
                    .append().query("UPDATE reputation_log SET donor_id = NULL WHERE donor_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.userId()));
            log.trace("Removed Data of user {}", task.userId());
        } else {
            builder = builder().query("DELETE FROM reputation_log WHERE guild_id = ? AND receiver_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()).setLong(task.userId()))
                    .append().query("UPDATE reputation_log SET donor_id = NULL WHERE guild_id = ? AND donor_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()).setLong(task.userId()));
            log.trace("Removed user reputation from guild {} of user {}", task.guildId(), task.userId());
        }

        builder.append().query("DELETE FROM cleanup_schedule WHERE task_id = ?;")
                .params(stmt -> stmt.setLong(1, task.taskId()))
                .update().executeSync();
    }
}
