package de.chojo.repbot.data;

import de.chojo.repbot.data.wrapper.RemovalTask;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import de.chojo.sqlutil.wrapper.stage.ResultStage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class GdprData extends QueryFactoryHolder {
    private static final Logger log = getLogger(GdprData.class);

    /**
     * Create a new QueryFactoryholder
     *
     * @param dataSource datasource
     */
    public GdprData(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder().build());
    }

    public void queueGuildDeletion(Guild guild) {
        builder()
                .query("""
                        INSERT INTO
                            cleanup_schedule(guild_id)
                            VALUES (?)
                                ON CONFLICT(guild_id, user_id)
                                    DO NOTHING;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update().executeSync();
    }

    public void queueGuildUserDeletion(User user, Guild guild) {
        builder()
                .query("""
                        INSERT INTO
                            cleanup_schedule(guild_id, user_id)
                            VALUES (?,?)
                                ON CONFLICT(guild_id, user_id)
                                    DO NOTHING;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(user.getIdLong()))
                .update().executeSync();
    }

    public void dequeueGuildDeletion(Guild guild) {
        builder()
                .query("DELETE FROM cleanup_schedule where guild_id = ?;")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update().executeSync();
    }

    public void dequeueGuildUserDeletion(Member member) {
        builder()
                .query("""
                        DELETE FROM
                            cleanup_schedule
                        where guild_id = ?
                            AND user_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(member.getGuild().getIdLong()).setLong(member.getIdLong()))
                .update().executeSync();
    }

    public void queueUserDeletion(User user) {
        builder()
                .query("""
                        INSERT INTO
                            cleanup_schedule(user_id, delete_after)
                            VALUES (?, now())
                                ON CONFLICT(guild_id, user_id)
                                    DO NOTHING;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(user.getIdLong()))
                .update().executeSync();
    }

    public List<RemovalTask> getRemovalTasks() {
        return builder(RemovalTask.class)
                .queryWithoutParams("""
                        SELECT
                            task_id,
                            user_id,
                            guild_id
                        from
                            cleanup_schedule
                        where delete_after < now();
                        """)
                .readRow(rs -> new RemovalTask(rs.getLong("task_id"), rs.getLong("guild_id"), rs.getLong("user_id")))
                .allSync();
    }

    public Optional<String> getUserData(User user) {
        return builder(String.class)
                .query("select repbot_schema.aggregate_user_data(?)")
                .paramsBuilder(stmt -> stmt.setLong(user.getIdLong()))
                .readRow(rs -> rs.getString(1))
                .firstSync();
    }

    public void executeRemovalTask(RemovalTask task) {
        ResultStage<Void> builder;
        if (task.userId() == null) {
            builder = builder().query("DELETE FROM reputation_log where guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM guild_bot_settings where guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM active_channel where guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM message_settings where guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM guild_ranks where guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM thankwords where guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()));
            log.info("Removed guild settings for {}", task.guildId());
        } else if (task.guildId() == null) {
            builder = builder().query("DELETE FROM reputation_log where receiver_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.userId()))
                    .append().query("UPDATE reputation_log SET donor_id = 0 where donor_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.userId()));
        } else {
            builder = builder().query("DELETE FROM reputation_log where guild_id = ? AND receiver_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()).setLong(task.userId()))
                    .append().query("UPDATE reputation_log SET donor_id = 0 where guild_id = ? AND donor_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()).setLong(task.userId()));
            log.info("Removed user reputation from guild {} of user {}", task.guildId(), task.userId());
        }

        builder.append().query("DELETE FROM cleanup_schedule where task_id = ?;")
                .params(stmt -> stmt.setLong(1, task.taskId()))
                .update().executeSync();
    }
}
