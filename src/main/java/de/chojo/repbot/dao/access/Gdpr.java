package de.chojo.repbot.dao.access;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.gdpr.GdprUser;
import de.chojo.repbot.dao.access.gdpr.RemovalTask;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

public class Gdpr extends QueryFactory {
    private static final Logger log = getLogger(Gdpr.class);
    private final Configuration configuration;

    public Gdpr(DataSource dataSource, Configuration configuration) {
        super(dataSource);
        this.configuration = configuration;
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
                .readRow(rs -> RemovalTask.build(this, rs))
                .allSync();
    }

    public void cleanupRequests() {
        builder()
                .query("""
                       DELETE FROM gdpr_log
                       WHERE received IS NOT NULL
                           AND received < NOW() - ?::INTERVAL;
                       """, configuration.cleanup().gdprDays())
                .parameter(stmt -> stmt.setString("%d DAYS".formatted(configuration.cleanup().gdprDays())))
                .update()
                .sendSync();
    }

    public GdprUser request(User user) {
        return new GdprUser(this, user);
    }

    /**
     * Get a list of {@link GdprUser}s which have requested their data.
     * <p>
     * Only users which are known to the provided shard manager will be returned.
     *
     * @param shardManager shardmanager to resolve users.
     * @return list of users
     */
    public List<GdprUser> getReportRequests(ShardManager shardManager) {
        return builder(GdprUser.class)
                .queryWithoutParams("""
                                        SELECT user_id 
                                        FROM gdpr_log
                                        WHERE received IS NULL
                                            AND last_attempt < NOW() - (LEAST(48, attempts) || ' HOURS')::interval 
                                    """)
                .readRow(rs -> GdprUser.build(this, rs, shardManager))
                .allSync()
                .stream()
                .filter(Objects::nonNull)
                .toList();
    }
}
