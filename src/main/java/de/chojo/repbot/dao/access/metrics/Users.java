/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.snapshots.statistics.UserStatistic;
import de.chojo.repbot.dao.snapshots.statistics.UsersStatistic;
import de.chojo.sadu.base.QueryFactory;

import java.util.concurrent.CompletableFuture;

public class Users extends QueryFactory {
    public Users(QueryFactory factoryHolder) {
        super(factoryHolder);
    }

    public CompletableFuture<UsersStatistic> week(int offset, int count) {
        return get("metrics_users_week", "week", offset, count);
    }

    public CompletableFuture<UsersStatistic> month(int offset, int count) {
        return get("metrics_users_month", "month", offset, count);
    }

    private CompletableFuture<UsersStatistic> get(String table, String timeframe, int offset, int count) {
        return builder(UserStatistic.class)
                .query("""
                       SELECT %s,
                           donor_count,
                           receiver_count,
                           total_count
                       FROM %s
                       WHERE %s <= DATE_TRUNC(?, NOW())::date - ?::interval
                       ORDER BY %s DESC
                       LIMIT ?
                       """, timeframe, table, timeframe, timeframe)
                .parameter(stmt -> stmt.setString(timeframe)
                                       .setString(offset + " " + timeframe).setInt(count))
                .readRow(rs -> UserStatistic.build(rs, timeframe))
                .all()
                .thenApply(UsersStatistic::new);
    }

    /**
     * Save the user count of the last week.
     */
    public void saveUserCountWeek() {
        builder()
                .query("""
                       INSERT INTO metrics_users_week
                       SELECT week, receiver_count, donor_count, total_count
                       FROM metrics_unique_users_week
                       WHERE week = DATE_TRUNC('week', NOW()  - INTERVAL '1 WEEK')
                       ON CONFLICT(week) 
                           DO UPDATE SET receiver_count = excluded.receiver_count,
                               donor_count = excluded.donor_count,
                               total_count = excluded.donor_count
                       """).emptyParams()
                .insert()
                .send();
    }

    /**
     * Save the user count of the last month.
     */
    public void saveUserCountMonth() {
        builder()
                .query("""
                       INSERT INTO metrics_users_month
                       SELECT month, receiver_count, donor_count, total_count
                       FROM metrics_unique_users_month
                       WHERE month = DATE_TRUNC('month', NOW()  - INTERVAL '1 MONTH')
                       ON CONFLICT(month) 
                           DO UPDATE SET receiver_count = excluded.receiver_count,
                               donor_count = excluded.donor_count,
                               total_count = excluded.donor_count
                       """)
                .emptyParams()
                .insert()
                .send();
    }
}
