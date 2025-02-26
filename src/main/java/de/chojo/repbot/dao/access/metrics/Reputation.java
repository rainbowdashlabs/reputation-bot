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

/**
 * Provides methods to retrieve and save reputation metrics.
 */
public class Reputation {
    /**
     * Constructs a Reputation object.
     */
    public Reputation() {
        super();
    }

    /**
     * Retrieves the total reputation counts for a specific week.
     *
     * @param week the week number
     * @param count the number of records to retrieve
     * @return the counts statistic for the specified week
     */
    public CountsStatistic totalWeek(int week, int count) {
        return get("metrics_reputation_total_week", "week", week, count);
    }

    /**
     * Retrieves the total reputation counts for a specific month.
     *
     * @param month the month number
     * @param count the number of records to retrieve
     * @return the counts statistic for the specified month
     */
    public CountsStatistic totalMonth(int month, int count) {
        return get("metrics_reputation_total_month", "month", month, count);
    }

    /**
     * Retrieves the reputation counts for a specific week.
     *
     * @param week the week number
     * @param count the number of records to retrieve
     * @return the counts statistic for the specified week
     */
    public CountsStatistic week(int week, int count) {
        return get("metrics_reputation_week", "week", week, count);
    }

    /**
     * Retrieves the reputation counts for a specific month.
     *
     * @param month the month number
     * @param count the number of records to retrieve
     * @return the counts statistic for the specified month
     */
    public CountsStatistic month(int month, int count) {
        return get("metrics_reputation_month", "month", month, count);
    }

    /**
     * Retrieves the reputation changes for a specific week.
     *
     * @param week the week number
     * @param count the number of records to retrieve
     * @return the labeled count statistic for the specified week
     */
    public LabeledCountStatistic weekChanges(int week, int count) {
        return getChanges("metrics_reputation_changed_week", "week", week, count);
    }

    /**
     * Retrieves the reputation changes for a specific month.
     *
     * @param month the month number
     * @param count the number of records to retrieve
     * @return the labeled count statistic for the specified month
     */
    public LabeledCountStatistic monthChanges(int month, int count) {
        return getChanges("metrics_reputation_changed_month", "month", month, count);
    }

    /**
     * Retrieves the reputation counts by type for a specific week.
     *
     * @param week the week number
     * @param count the number of records to retrieve
     * @return the labeled count statistic for the specified week
     */
    public LabeledCountStatistic typeWeek(int week, int count) {
        return getType("metrics_reputation_type_week", "week", week, count);
    }

    /**
     * Retrieves the reputation counts by type for a specific month.
     *
     * @param month the month number
     * @param count the number of records to retrieve
     * @return the labeled count statistic for the specified month
     */
    public LabeledCountStatistic typeMonth(int month, int count) {
        return getType("metrics_reputation_type_month", "month", month, count);
    }

    /**
     * Retrieves the total reputation counts by type for a specific week.
     *
     * @param week the week number
     * @param count the number of records to retrieve
     * @return the labeled count statistic for the specified week
     */
    public LabeledCountStatistic typeTotalWeek(int week, int count) {
        return getType("metrics_reputation_type_total_week", "week", week, count);
    }

    /**
     * Retrieves the total reputation counts by type for a specific month.
     *
     * @param month the month number
     * @param count the number of records to retrieve
     * @return the labeled count statistic for the specified month
     */
    public LabeledCountStatistic typeTotalMonth(int month, int count) {
        return getType("metrics_reputation_type_total_month", "month", month, count);
    }

    /**
     * Retrieves the reputation counts by day of the week for a specific week.
     *
     * @param week the week number
     * @return the day of the week statistic for the specified week
     */
    public DowsStatistic dowWeek(int week) {
        return get("metrics_reputation_dow_week", "week", week);
    }

    /**
     * Retrieves the reputation counts by day of the week for a specific month.
     *
     * @param month the month number
     * @return the day of the week statistic for the specified month
     */
    public DowsStatistic dowMonth(int month) {
        return get("metrics_reputation_dow_month", "month", month);
    }

    /**
     * Retrieves the reputation counts by day of the week for a specific year.
     *
     * @param year the year number
     * @return the day of the week statistic for the specified year
     */
    public DowsStatistic dowYear(int year) {
        return get("metrics_reputation_dow_year", "year", year);
    }

    /**
     * Retrieves the counts statistic for a specific timeframe.
     *
     * @param table the table name
     * @param timeframe the timeframe column name
     * @param offset the offset value
     * @param count the number of records to retrieve
     * @return the counts statistic
     */
    private CountsStatistic get(String table, String timeframe, int offset, int count) {
        return Query.query("""
                            SELECT %s,
                                count
                            FROM %s
                            WHERE %s <= date_trunc(?, now())::DATE - ?::INTERVAL
                            ORDER BY %s DESC
                            LIMIT ?
                            """, timeframe, table, timeframe, timeframe)
                    .single(call().bind(timeframe)
                                  .bind(offset + " " + timeframe)
                                  .bind(count))
                    .map(rs -> CountStatistics.build(rs, timeframe))
                    .allResults()
                    .map(CountsStatistic::new);
    }

    /**
     * Retrieves the labeled count statistic by type for a specific timeframe.
     *
     * @param table the table name
     * @param timeframe the timeframe column name
     * @param offset the offset value
     * @param count the number of records to retrieve
     * @return the labeled count statistic
     */
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

    /**
     * Retrieves the labeled count statistic for changes for a specific timeframe.
     *
     * @param table the table name
     * @param timeframe the timeframe column name
     * @param offset the offset value
     * @param count the number of records to retrieve
     * @return the labeled count statistic
     */
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

    /**
     * Retrieves the day of the week statistic for a specific timeframe.
     *
     * @param table the table name
     * @param timeframe the timeframe column name
     * @param offset the offset value
     * @return the day of the week statistic
     */
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

    /**
     * Saves the reputation counts of the previous day into metric tables.
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
                     """)
             .single()
             .insert();
        Query.query("""
                     INSERT INTO metrics_reputation_count(day, count)
                     SELECT now() - INTERVAL '1 DAY',
                     count(1)
                     FROM reputation_log
                     WHERE received < now()::DATE
                     ON CONFLICT(day)
                     DO UPDATE
                     SET count = excluded.count;
                     """)
             .single()
             .insert();
    }
}
