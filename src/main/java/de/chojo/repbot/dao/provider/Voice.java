package de.chojo.repbot.dao.provider;

import de.chojo.repbot.util.LogNotify;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import de.chojo.sqlutil.wrapper.stage.ResultStage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class Voice extends QueryFactoryHolder {
    private static final Logger log = getLogger(Voice.class);

    public Voice(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder().withExceptionHandler(e ->
                        log.error(LogNotify.NOTIFY_ADMIN, ExceptionTransformer.prettyException("Query execution failed", e), e))
                .build());
    }

    /**
     * Log that a user was in a channel with another user.
     *
     * @param source the user which has seen the users.
     * @param seen   the members which were seen by the user lately
     */
    public void logUser(Member source, List<Member> seen) {
        var baseId = source.getIdLong();
        var builder = builder();
        ResultStage<Void> resultStage = null;
        for (var user : seen) {
            var otherId = user.getIdLong();
            resultStage = builder.query("""
                            INSERT INTO voice_activity(relation_key, guild_id, user_id_1, user_id_2) VALUES (?,?,?,?)
                                ON CONFLICT(relation_key, guild_id)
                                    DO UPDATE
                                        SET seen = NOW()
                            """)
                    .paramsBuilder(stmt -> stmt.setLong(baseId ^ otherId).setLong(source.getGuild().getIdLong())
                            .setLong(baseId).setLong(otherId));
        }
        if (resultStage == null) return;
        resultStage.update().executeSync();
    }

    /**
     * Retrieve the last users which were in a voice channel with the requested user in the last minutes.
     *
     * @param user    user to retrieve other users for
     * @param guild   guild to check
     * @param minutes the amount of past minutes
     * @param limit   max number of returned ids
     * @return list of ids
     */
    public List<Long> getPastUser(User user, Guild guild, int minutes, int limit) {
        return builder(Long.class)
                .query("""
                        SELECT
                            user_id_1, user_id_2
                        FROM
                            voice_activity
                        WHERE
                         guild_id = ?
                         AND (user_id_1 = ?
                            OR user_id_2 = ?
                         )
                         AND seen > NOW() - (? || 'minute')::interval
                        ORDER BY
                            seen DESC
                        LIMIT ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(user.getIdLong()).setLong(user.getIdLong())
                        .setInt(minutes).setInt(limit))
                .readRow(rs -> {
                    var id1 = rs.getLong("user_id_1");
                    var id2 = rs.getLong("user_id_2");
                    return id1 == user.getIdLong() ? id2 : id1;
                }).allSync();
    }

    /**
     * Cleanup the voice activity
     */
    public void cleanup() {
        builder()
                .queryWithoutParams("""
                        DELETE FROM voice_activity WHERE seen < NOW() - '12 hours'::interval
                        """)
                .update().execute();
    }
}
