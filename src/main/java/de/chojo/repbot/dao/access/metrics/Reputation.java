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
import de.chojo.sadu.queries.api.query.Query;

import static de.chojo.sadu.queries.api.call.Call.call;

public class Reputation {
    public Reputation() {
        super();
    }

    public CountsStatistic totalWeek(int week, int count) {
        return get("metrics_reputation_total_week", "week", week, count);
    }

    public CountsStatistic totalMonth(int month, int count) {
        return get("metrics_reputation_total_month", "month", month, count);
    }

    public CountsStatistic week(int week, int count) {
        return get("metrics_reputation_week", "week", week, count);
    }

    public CountsStatistic month(int month, int count) {
        return get("metrics_reputation_month", "month", month, count);
    }

    public LabeledCountStatistic weekChanges(int week, int count) {
        return getChanges("metrics_reputation_changed_week", "week", week, count);
    }

    public LabeledCountStatistic monthChanges(int month, int count) {
        return getChanges("metrics_reputation_changed_month", "month", month, count);
    }

    public LabeledCountStatistic typeWeek(int week, int count) {
        return getType("metrics_reputation_type_week", "week", week, count);
    }

    public LabeledCountStatistic typeMonth(int month, int count) {
        return getType("metrics_reputation_type_month", "month", month, count);
    }

    public LabeledCountStatistic typeTotalWeek(int week, int count) {
        return getType("metrics_reputation_type_total_week", "week", week, count);
    }

    public LabeledCountStatistic typeTotalMonth(int month, int count) {
        return getType("metrics_reputation_type_total_month", "month", month, count);
    }

    public DowsStatistic dowWeek(int week) {
        return get("metrics_reputation_dow_week", "week", week);
    }

    public DowsStatistic dowMonth(int month) {
        return get("metrics_reputation_dow_month", "month", month);
    }

    public DowsStatistic dowYear(int year) {
        return get("metrics_reputation_dow_year", "year", year);
    }

    /**
     * Save reputation counts of the previous day into metric tables.
     */
    public void saveRepCounts() {
        Query.query("""
                     INSERT INTO metrics_reputation(day, cause, count)
                     SELECT received::DATE AS day,
                            cause,
                            count(1)       AS count
                     FROM reputation_log
                     WHERE received::DATE = now()::DATE - INTERVAL '1 DAY'
                     GROUP BY day, cause
                     ORDER BY day DESC
                     ON CONFLICT(day, cause)
                     DO UPDATE
                     SET count = excluded.count;
                     """).single().insert();
        Query.query("""
                     INSERT INTO metrics_reputation_count(day, count)
                     SELECT now() - INTERVAL '1 DAY',
                     count(1)
                     FROM reputation_log
                     WHERE received < now()::DATE
                     ON CONFLICT(day)
                     DO UPDATE
                     SET count = excluded.count;
                     """).single().insert();
    }

    private CountsStatistic get(String table, String timeframe, int offset, int count) {
        return Query.query("""
                            SELECT %s,
                                count
                            FROM %s
                            WHERE %s <= date_trunc(?, now())::DATE - ?::INTERVAL
                            ORDER BY %s DESC
                            LIMIT ?
                            """, timeframe, table, timeframe, timeframe)
                .single(call().bind(timeframe).bind(offset + " " + timeframe).bind(count))
                .map(rs -> CountStatistics.build(rs, timeframe))
                .allResults()
                .map(CountsStatistic::new);
    }

    private LabeledCountStatistic getType(String table, String timeframe, int offset, int count) {
        var builder = new LabeledCountStatisticBuilder();
        Query.query("""
                     SELECT %s,
                         cause,
                         count
                     FROM %s
                     WHERE %s <= date_trunc(?, now())::DATE - ?::INTERVAL
                     ORDER BY %s DESC
                     LIMIT ?
                     """, timeframe, table, timeframe, timeframe)
                .single(call().bind(timeframe).bind(offset + " " + timeframe).bind(count))
                .map(rs -> builder.add(rs.getString("cause"), CountStatistics.build(rs, timeframe)))
                .all();
        return builder.build();
    }

    private LabeledCountStatistic getChanges(String table, String timeframe, int offset, int count) {
        var builder = new LabeledCountStatisticBuilder();
        Query.query("""
                     SELECT %s,
                         added - removed AS delta,
                         added,
                         removed
                     FROM %s
                     WHERE %s <= date_trunc(?, now())::DATE - ?::INTERVAL
                     ORDER BY %s DESC
                     LIMIT ?
                     """, timeframe, table, timeframe, timeframe)
                .single(call().bind(timeframe).bind(offset + " " + timeframe).bind(count))
                .map(rs -> builder.add("delta", CountStatistics.build(rs, "delta", timeframe))
                        .add("added", CountStatistics.build(rs, "added", timeframe))
                        .add("removed", CountStatistics.build(rs, "removed", timeframe)))
                .all();
        return builder.build();
    }

    private DowsStatistic get(String table, String timeframe, int offset) {
        return Query.query("""
                            SELECT %s,
                                dow,
                                count
                            FROM %s
                            WHERE %s = date_trunc(?, now())::DATE - ?::INTERVAL
                            ORDER BY %s DESC
                            """, timeframe, table, timeframe, timeframe)
                .single(call().bind(timeframe).bind(offset + " " + timeframe))
                .map(rs -> DowStatistics.build(rs, timeframe))
                .allResults()
                .map(DowsStatistic::new);
    }
}
