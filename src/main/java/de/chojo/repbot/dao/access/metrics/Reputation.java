/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.snapshots.statistics.CountStatistics;
import de.chojo.repbot.dao.snapshots.statistics.CountsStatistic;
import de.chojo.repbot.dao.snapshots.statistics.DowStatistics;
import de.chojo.repbot.dao.snapshots.statistics.DowsStatistic;
import de.chojo.repbot.dao.snapshots.statistics.LabeledCountStatistic;
import de.chojo.repbot.dao.snapshots.statistics.builder.LabeledCountStatisticBuilder;
import de.chojo.sadu.base.QueryFactory;

import java.util.concurrent.CompletableFuture;

public class Reputation extends QueryFactory {
    public Reputation(QueryFactory factoryHolder) {
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

    public CompletableFuture<LabeledCountStatistic> weekChanges(int week, int count) {
        return getChanges("metrics_reputation_changed_week", "week", week, count);
    }

    public CompletableFuture<LabeledCountStatistic> monthChanges(int month, int count) {
        return getChanges("metrics_reputation_changed_month", "month", month, count);
    }

    public CompletableFuture<LabeledCountStatistic> typeWeek(int week, int count) {
        return getType("metrics_reputation_type_week", "week", week, count);
    }

    public CompletableFuture<LabeledCountStatistic> typeMonth(int month, int count) {
        return getType("metrics_reputation_type_month", "month", month, count);
    }

    public CompletableFuture<LabeledCountStatistic> typeTotalWeek(int week, int count) {
        return getType("metrics_reputation_type_total_week", "week", week, count);
    }

    public CompletableFuture<LabeledCountStatistic> typeTotalMonth(int month, int count) {
        return getType("metrics_reputation_type_total_month", "month", month, count);
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
        return builder(CountStatistics.class)
                .query("""
                       SELECT %s,
                           count
                       FROM %s
                       WHERE %s <= DATE_TRUNC(?, NOW())::date - ?::interval
                       ORDER BY %s DESC
                       LIMIT ?
                       """, timeframe, table, timeframe, timeframe)
                .parameter(stmt -> stmt.setString(timeframe)
                                       .setString(offset + " " + timeframe).setInt(count))
                .readRow(rs -> CountStatistics.build(rs, timeframe))
                .all()
                .thenApply(CountsStatistic::new);
    }

    private CompletableFuture<LabeledCountStatistic> getType(String table, String timeframe, int offset, int count) {
        var builder = new LabeledCountStatisticBuilder();
        return builder(LabeledCountStatisticBuilder.class)
                .query("""
                       SELECT %s,
                           cause,
                           count
                       FROM %s
                       WHERE %s <= DATE_TRUNC(?, NOW())::date - ?::interval
                       ORDER BY %s DESC
                       LIMIT ?
                       """, timeframe, table, timeframe, timeframe)
                .parameter(stmt -> stmt.setString(timeframe)
                                       .setString(offset + " " + timeframe)
                                       .setInt(count))
                .readRow(rs -> builder.add(rs.getString("cause"), CountStatistics.build(rs, timeframe)))
                .all()
                .thenApply(r -> builder.build());
    }

    private CompletableFuture<LabeledCountStatistic> getChanges(String table, String timeframe, int offset, int count) {
        var builder = new LabeledCountStatisticBuilder();
        return builder(LabeledCountStatisticBuilder.class)
                .query("""
                       SELECT %s,
                           added - removed as delta,
                           added,
                           removed
                       FROM %s
                       WHERE %s <= DATE_TRUNC(?, NOW())::date - ?::interval
                       ORDER BY %s DESC
                       LIMIT ?
                       """, timeframe, table, timeframe, timeframe)
                .parameter(stmt -> stmt.setString(timeframe)
                                       .setString(offset + " " + timeframe)
                                       .setInt(count))
                .readRow(rs -> builder.add("delta", CountStatistics.build(rs, "delta",timeframe))
                                      .add("added", CountStatistics.build(rs, "added",timeframe))
                                      .add("removed", CountStatistics.build(rs, "removed",timeframe)))
                .all()
                .thenApply(r -> builder.build());
    }

    private CompletableFuture<DowsStatistic> get(String table, String timeframe, int offset) {
        return builder(DowStatistics.class)
                .query("""
                       SELECT %s,
                           dow,
                           count
                       FROM %s
                       WHERE %s = DATE_TRUNC(?, NOW())::date - ?::interval
                       ORDER BY %s DESC
                       """, timeframe, table, timeframe, timeframe)
                .parameter(stmt -> stmt.setString(timeframe)
                                       .setString(offset + " " + timeframe))
                .readRow(rs -> DowStatistics.build(rs, timeframe))
                .all()
                .thenApply(DowsStatistic::new);
    }

    /**
     * Save reputation counts of the previous day into metric tables.
     */
    public void saveRepCounts() {
        builder()
                .query("""
                       INSERT INTO metrics_reputation(day, cause, count)
                       SELECT received::date AS day,
                              cause,
                              COUNT(1)       AS count
                       FROM reputation_log
                       WHERE received::date = NOW()::date - INTERVAL '1 DAY'
                       GROUP BY day, cause
                       ORDER BY day DESC
                       ON CONFLICT(day, cause)
                       DO UPDATE
                       SET count = excluded.count;
                       """)
                .emptyParams()
                .append()
                .query("""
                       INSERT INTO metrics_reputation_count(day, count)
                       SELECT NOW() - INTERVAL '1 DAY',
                       COUNT(1) 
                       FROM reputation_log
                       WHERE received < NOW()::date
                       ON CONFLICT(day) 
                       DO UPDATE
                       SET count = excluded.count;
                       """)
                .emptyParams()
                .insert()
                .send();
    }
}
