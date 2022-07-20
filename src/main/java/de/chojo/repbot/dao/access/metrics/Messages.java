package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.snapshots.statistics.CountStatistics;
import de.chojo.repbot.dao.snapshots.statistics.CountsStatistic;
import de.chojo.sqlutil.base.QueryFactoryHolder;

import java.util.concurrent.CompletableFuture;

public class Messages extends QueryFactoryHolder {
    public Messages(QueryFactoryHolder factoryHolder) {
        super(factoryHolder);
    }

    public void countMessage() {
        builder().queryWithoutParams("""
                        INSERT INTO metrics_message_analyzed(hour, count) VALUES (date_trunc('hour', now()), 1)
                        ON CONFLICT(hour)
                            DO UPDATE SET count = metrics_message_analyzed.count + 1
                        """)
                .insert()
                .execute();
    }

    public CompletableFuture<CountsStatistic> hour(int hour, int count) {
        return get("metrics_message_analyzed", "hour", hour, count);
    }

    public CompletableFuture<CountsStatistic> day(int day, int count) {
        return get("metrics_message_analyzed_day", "day", day, count);
    }

    public CompletableFuture<CountsStatistic> week(int week, int count) {
        return get("metrics_message_analyzed_week", "week", week, count);
    }

    public CompletableFuture<CountsStatistic> month(int month, int count) {
        return get("metrics_message_analyzed_month", "month", month, count);
    }

    public CompletableFuture<CountsStatistic> totalDay(int day, int count) {
        return get("metrics_messages_analyzed_total_day", "day", day, count);
    }

    public CompletableFuture<CountsStatistic> totalWeek(int week, int count) {
        return get("metrics_messages_analyzed_total_week", "week", week, count);
    }

    public CompletableFuture<CountsStatistic> totalMonth(int month, int count) {
        return get("metrics_messages_analyzed_total_month", "month", month, count);
    }

    private CompletableFuture<CountsStatistic> get(String table, String timeframe, int offset, int count) {
        return builder(CountStatistics.class).query("""
                        SELECT %s,
                            count
                        FROM %s
                        WHERE %s <= DATE_TRUNC(?, NOW()) - ?::INTERVAL
                        ORDER BY %s DESC
                        LIMIT ?
                        """, timeframe, table, timeframe, timeframe)
                .paramsBuilder(stmt -> stmt.setString(timeframe).setString(offset + " " + timeframe).setInt(count))
                .readRow(rs -> CountStatistics.build(rs, timeframe))
                .all()
                .thenApply(CountsStatistic::new);
    }
}
