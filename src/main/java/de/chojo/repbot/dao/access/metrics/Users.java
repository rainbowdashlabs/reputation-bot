package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.snapshots.statistics.UserStatistic;
import de.chojo.sqlutil.base.QueryFactoryHolder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Users extends QueryFactoryHolder {
    public Users(QueryFactoryHolder factoryHolder) {
        super(factoryHolder);
    }

    public CompletableFuture<List<UserStatistic>> week(int week) {
        return get("metrics_message_analyzed_week", "week", week);
    }

    public CompletableFuture<List<UserStatistic>> month(int month) {
        return get("metrics_message_analyzed_month", "month", month);
    }

    private CompletableFuture<List<UserStatistic>> get(String table, String timeframe, int offset) {
        return builder(UserStatistic.class).query("""
                        SELECT %s,
                            donor_count,
                            receiver_count
                        FROM %s
                        WHERE %s = DATE_TRUNC('%s', NOW())::date - INTERVAL ?
                        """, timeframe, table, timeframe, timeframe).paramsBuilder(stmt -> stmt.setString(offset + " " + timeframe))
                .readRow(rs -> UserStatistic.build(rs, timeframe))
                .all();
    }
}
