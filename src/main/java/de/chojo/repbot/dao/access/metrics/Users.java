/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.snapshots.statistics.UserStatistic;
import de.chojo.repbot.dao.snapshots.statistics.UsersStatistic;
import de.chojo.sadu.queries.api.query.Query;

import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;

/**
 * Class for accessing user metrics data.
 */
public class Users {
    /**
     * Constructs a new Users instance.
     */
    public Users() {
        super();
    }

    /**
     * Retrieves user statistics for the specified week.
     *
     * @param offset the offset for the week
     * @param count the number of weeks to retrieve
     * @return the user statistics for the specified week
     */
    public UsersStatistic week(int offset, int count) {
        return get("metrics_users_week", "week", offset, count);
    }

    /**
     * Retrieves user statistics for the specified month.
     *
     * @param offset the offset for the month
     * @param count the number of months to retrieve
     * @return the user statistics for the specified month
     */
    public UsersStatistic month(int offset, int count) {
        return get("metrics_users_month", "month", offset, count);
    }

    /**
     * Retrieves user statistics for the specified timeframe.
     *
     * @param table the table to query
     * @param timeframe the timeframe to query (week or month)
     * @param offset the offset for the timeframe
     * @param count the number of timeframes to retrieve
     * @return the user statistics for the specified timeframe
     */
    private UsersStatistic get(String table, String timeframe, int offset, int count) {
        return Query.query("""
                            SELECT %s,
                                donor_count,
                                receiver_count,
                                total_count
                            FROM %s
                            WHERE %s <= date_trunc(?, now())::DATE - ?::INTERVAL
                            ORDER BY %s DESC
                            LIMIT ?
                            """, timeframe, table, timeframe, timeframe)
                    .single(call().bind(timeframe).bind(offset + " " + timeframe).bind(count))
                    .map(rs -> UserStatistic.build(rs, timeframe))
                    .allResults()
                    .map(UsersStatistic::new);
    }

    /**
     * Saves the user count of the last week.
     */
    public void saveUserCountWeek() {
        Query.query("""
                     INSERT INTO metrics_users_week
                     SELECT week, receiver_count, donor_count, total_count
                     FROM metrics_unique_users_week
                     WHERE week = date_trunc('week', now()  - INTERVAL '1 WEEK')
                     ON CONFLICT(week)
                         DO UPDATE SET receiver_count = excluded.receiver_count,
                             donor_count = excluded.donor_count,
                             total_count = excluded.donor_count
                     """)
             .single()
             .insert();
    }

    /**
     * Saves the user count of the last month.
     */
    public void saveUserCountMonth() {
        Query.query("""
                     INSERT INTO metrics_users_month
                     SELECT month, receiver_count, donor_count, total_count
                     FROM metrics_unique_users_month
                     WHERE month = date_trunc('month', now()  - INTERVAL '1 MONTH')
                     ON CONFLICT(month)
                         DO UPDATE SET receiver_count = excluded.receiver_count,
                             donor_count = excluded.donor_count,
                             total_count = excluded.donor_count
                     """)
             .single()
             .insert();
    }
}
