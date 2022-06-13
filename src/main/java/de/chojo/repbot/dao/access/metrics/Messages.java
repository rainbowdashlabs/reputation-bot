package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.snapshots.statistics.CountStatistics;
import de.chojo.sqlutil.base.QueryFactoryHolder;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public class Messages extends QueryFactoryHolder {
    public Messages(QueryFactoryHolder factoryHolder) {
        super(factoryHolder);
    }

    public void countMessage() {
        builder().queryWithoutParams("""
                        INSERT INTO metrics_message_analyzed(hour, count) VALUES (date_trunc('hour', now()), ?)
                        ON CONFLICT(hour)
                            DO UPDATE SET count = count + 1
                        """)
                .insert()
                .execute();
    }

    public CompletableFuture<CountStatistics> hour(int hour) {
        return get("metrics_message_analyzed", "hour", hour);
    }

    public CompletableFuture<CountStatistics> day(int day) {
        return get("metrics_message_analyzed_day", "day", day);
    }

    public CompletableFuture<CountStatistics> week(int week) {
        return get("metrics_message_analyzed_week", "week", week);
    }

    public CompletableFuture<CountStatistics> month(int month) {
        return get("metrics_unique_users_month", "month", month);
    }

    private CompletableFuture<CountStatistics> get(String table, String timeframe, int offset) {
        return builder(CountStatistics.class).query("""
                        SELECT %s,
                            count
                        FROM %s
                        WHERE %s = DATE_TRUNC('%s', NOW())::DATE - ?::INTERVAL
                        """, timeframe, table, timeframe, timeframe).paramsBuilder(stmt -> stmt.setString(offset + " " + timeframe))
                .readRow(rs -> new CountStatistics(rs.getDate(timeframe).toLocalDate(), rs.getInt("count")))
                .first()
                .thenApply(r -> r.orElse(new CountStatistics(LocalDate.MIN, 0)));
    }
}
