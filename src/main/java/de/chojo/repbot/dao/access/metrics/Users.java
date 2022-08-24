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
        return get("metrics_unique_users_week", "week", offset, count);
    }

    public CompletableFuture<UsersStatistic> month(int offset, int count) {
        return get("metrics_unique_users_month", "month", offset, count);
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
}
