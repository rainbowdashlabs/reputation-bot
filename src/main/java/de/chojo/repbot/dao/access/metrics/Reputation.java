package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.snapshots.statistics.CountStatistics;
import de.chojo.repbot.dao.snapshots.statistics.CountsStatistic;
import de.chojo.repbot.dao.snapshots.statistics.DowStatistics;
import de.chojo.repbot.dao.snapshots.statistics.DowsStatistic;
import de.chojo.sqlutil.base.QueryFactoryHolder;

import java.util.concurrent.CompletableFuture;

public class Reputation extends QueryFactoryHolder {
    public Reputation(QueryFactoryHolder factoryHolder) {
        super(factoryHolder);
    }

    public CompletableFuture<CountsStatistic> totalWeek(int week, int count) {
        return get("metrics_reputation_total_week", "week", week, count);
    }

    public CompletableFuture<CountsStatistic> totalMonth(int month, int count) {
        return get("metrics_reputation_total_month", "month", month, count);
    }

    public CompletableFuture<CountsStatistic> week(int week, int count) {
        return get("metrics_reputation_week", "week", week, count);
    }

    public CompletableFuture<CountsStatistic> month(int month, int count) {
        return get("metrics_reputation_month", "month", month, count);
    }

    public CompletableFuture<DowsStatistic> dowWeek(int week) {
        return get("metrics_reputation_dow_week", "week", week);
    }

    public CompletableFuture<DowsStatistic> dowMonth(int month) {
        return get("metrics_reputation_dow_month", "month", month);
    }

    public CompletableFuture<DowsStatistic> dowYear(int year) {
        return get("metrics_reputation_dow_year", "year", year);
    }

    private CompletableFuture<CountsStatistic> get(String table, String timeframe, int offset, int count) {
        return builder(CountStatistics.class).query("""
                        SELECT %s,
                            count
                        FROM %s
                        WHERE %s <= DATE_TRUNC(?, NOW())::date - ?::interval
                        ORDER BY %s DESC
                        LIMIT ?
                        """, timeframe, table, timeframe, timeframe)
                .paramsBuilder(stmt -> stmt.setString(timeframe).setString(offset + " " + timeframe).setInt(count))
                .readRow(rs -> CountStatistics.build(rs, timeframe))
                .all()
                .thenApply(CountsStatistic::new);
    }

    private CompletableFuture<DowsStatistic> get(String table, String timeframe, int offset) {
        return builder(DowStatistics.class).query("""
                        SELECT %s,
                            dow,
                            count
                        FROM %s
                        WHERE %s = DATE_TRUNC(?, NOW())::date - ?::interval
                        ORDER BY %s DESC
                        """, timeframe, table, timeframe, timeframe)
                .paramsBuilder(stmt -> stmt.setString(timeframe).setString(offset + " " + timeframe))
                .readRow(rs -> DowStatistics.build(rs, timeframe))
                .all()
                .thenApply(DowsStatistic::new);
    }
}
