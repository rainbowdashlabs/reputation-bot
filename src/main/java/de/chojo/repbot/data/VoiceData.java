package de.chojo.repbot.data;

import de.chojo.jdautil.database.QueryObject;
import de.chojo.jdautil.database.builder.QueryBuilderConfig;
import de.chojo.jdautil.database.builder.QueryBuilderFactory;
import de.chojo.jdautil.database.builder.stage.ResultStage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import javax.sql.DataSource;
import java.util.List;

public class VoiceData extends QueryObject {
    private final QueryBuilderFactory factory;

    public VoiceData(DataSource dataSource) {
        super(dataSource);
        factory = new QueryBuilderFactory(QueryBuilderConfig.builder().build(), dataSource);
    }

    /**
     * Log that a user was in a channel with another user.
     *
     * @param source the user which has seen the users.
     * @param seen   the members which were seen by the user lately
     */
    public void logUser(Member source, List<Member> seen) {
        var baseId = source.getIdLong();
        var builder = factory.builder();
        ResultStage<Void> resultStage = null;
        for (var user : seen) {
            long otherId = user.getIdLong();
            resultStage = builder.query("""
                    INSERT INTO voice_activity(relation_key, guild_id, user_id_1, user_id_2) VALUES (?,?,?,?)
                        ON CONFLICT(relation_key, guild_id)
                            DO UPDATE
                                set seen = now()
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
        return factory.builder(Long.class)
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
                         AND seen > now() - (? || 'minute')::interval
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
        factory.builder()
                .queryWithoutParams("""
                        DELETE FROM voice_activity WHERE seen < now() - '12 hours'::interval
                        """)
                .update().execute();
    }
}
