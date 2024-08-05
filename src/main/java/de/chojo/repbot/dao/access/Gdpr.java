/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.gdpr.GdprUser;
import de.chojo.repbot.dao.access.gdpr.RemovalTask;
import de.chojo.sadu.queries.api.query.Query;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

import static de.chojo.sadu.queries.api.call.Call.call;
import static org.slf4j.LoggerFactory.getLogger;

public class Gdpr {
    private static final Logger log = getLogger(Gdpr.class);
    private final Configuration configuration;

    public Gdpr(Configuration configuration) {
        this.configuration = configuration;
    }

    public List<RemovalTask> getRemovalTasks() {
        return Query.query("""
                            SELECT
                                task_id,
                                user_id,
                                guild_id
                            FROM
                                cleanup_schedule
                            WHERE delete_after < now();
                            """)
                    .single()
                    .map(RemovalTask::build)
                    .all();
    }

    public void cleanupRequests() {
        Query.query("""
                     DELETE FROM gdpr_log
                     WHERE received IS NOT NULL
                         AND received < now() - ?::INTERVAL;
                     """, configuration.cleanup().gdprDays())
             .single(call().bind("%d DAYS".formatted(configuration.cleanup().gdprDays())))
             .update();
    }

    public GdprUser request(User user) {
        return new GdprUser(user);
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
        return Query
                .query("""
                            SELECT user_id
                            FROM gdpr_log
                            WHERE received IS NULL
                                AND last_attempt < now() - (least(48, attempts) || ' HOURS')::INTERVAL 
                        """)
                .single()
                .map(rs -> GdprUser.build(rs, shardManager))
                .all()
                .stream()
                .filter(Objects::nonNull)
                .toList();
    }
}
