/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.snapshots.statistics.CountStatistics;
import de.chojo.repbot.dao.snapshots.statistics.LabeledCountStatistic;
import de.chojo.repbot.dao.snapshots.statistics.builder.LabeledCountStatisticBuilder;
import de.chojo.sadu.queries.api.query.Query;

import static de.chojo.sadu.queries.api.call.Call.call;

/**
 * Service class for handling metrics related to interactions.
 */
public class Service {
    /**
     * Constructs a new Service.
     */
    public Service() {
        super();
    }

    /**
     * Logs a successful interaction.
     */
    public void successfulInteraction() {
        logInteraction("success");
    }

    /**
     * Logs a failed interaction.
     */
    public void failedInteraction() {
        logInteraction("failed");
    }

    /**
     * Logs a counted interaction.
     */
    public void countInteraction() {
        logInteraction("count");
    }

    /**
     * Logs an interaction of the specified type.
     *
     * @param type the type of interaction to log
     */
    private void logInteraction(String type) {
        Query.query("""
                     INSERT INTO metrics_handled_interactions(hour, %s) VALUES (date_trunc('hour', now()), 1)
                     ON CONFLICT(hour)
                         DO UPDATE SET %s = metrics_handled_interactions.%s + 1
                     """, type, type, type)
             .single()
             .insert();
    }

    /**
     * Retrieves the labeled count statistic for the specified hour.
     *
     * @param hour the hour to retrieve the statistic for
     * @param count the number of records to retrieve
     * @return the labeled count statistic
     */
    public LabeledCountStatistic hour(int hour, int count) {
        return get("metrics_handled_interactions", "hour", hour, count);
    }

    /**
     * Retrieves the labeled count statistic for the specified day.
     *
     * @param day the day to retrieve the statistic for
     * @param count the number of records to retrieve
     * @return the labeled count statistic
     */
    public LabeledCountStatistic day(int day, int count) {
        return get("metrics_handled_interactions_day", "day", day, count);
    }

    /**
     * Retrieves the labeled count statistic for the specified week.
     *
     * @param week the week to retrieve the statistic for
     * @param count the number of records to retrieve
     * @return the labeled count statistic
     */
    public LabeledCountStatistic week(int week, int count) {
        return get("metrics_handled_interactions_week", "week", week, count);
    }

    /**
     * Retrieves the labeled count statistic for the specified month.
     *
     * @param month the month to retrieve the statistic for
     * @param count the number of records to retrieve
     * @return the labeled count statistic
     */
    public LabeledCountStatistic month(int month, int count) {
        return get("metrics_handled_interactions_month", "month", month, count);
    }

    /**
     * Retrieves the labeled count statistic for the specified timeframe.
     *
     * @param table the table to query
     * @param timeframe the timeframe to retrieve the statistic for
     * @param offset the offset to apply to the timeframe
     * @param count the number of records to retrieve
     * @return the labeled count statistic
     */
    private LabeledCountStatistic get(String table, String timeframe, int offset, int count) {
        var builder = new LabeledCountStatisticBuilder();
        return Query.query("""
                        SELECT %s,
                            count,
                            failed,
                            success
                        FROM %s
                        WHERE %s <= DATE_TRUNC(?, NOW()) - ?::interval
                        ORDER BY %s DESC
                        LIMIT ?
                        """, timeframe, table, timeframe, timeframe)
                .single(call().bind(timeframe)
                                       .bind(offset + " " + timeframe)
                                       .bind(count))
                .map(rs -> builder.add("count", CountStatistics.build(rs, "count", timeframe))
                                      .add("success", CountStatistics.build(rs, "success", timeframe))
                                      .add("failed", CountStatistics.build(rs, "failed", timeframe))
                )
                .allResults()
                .map(r -> builder.build());
    }
}
