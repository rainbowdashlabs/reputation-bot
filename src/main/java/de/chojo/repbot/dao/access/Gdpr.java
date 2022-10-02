package de.chojo.repbot.dao.access;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.gdpr.GdprUser;
import de.chojo.repbot.dao.access.gdpr.RemovalTask;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.List;

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
                .queryWithoutParams("""
                                    DELETE FROM gdpr_log
                                    WHERE received IS NOT NULL
                                        AND received < NOW() - INTERVAL '%d DAYS';
                                    """, configuration.cleanup().gdprDays())
                .update()
                .sendSync();
    }

    public GdprUser request(User user) {
        return new GdprUser(this, user.getIdLong());
    }

    /**
     * Get a list of {@link GdprUser}s which have requested their data.
     *
     * @return list of users
     */
    public List<GdprUser> getReportRequests() {
        return builder(GdprUser.class)
                .queryWithoutParams("SELECT user_id FROM gdpr_log WHERE received IS NULL")
                .readRow(rs -> GdprUser.build(this, rs))
                .allSync();
    }
}
