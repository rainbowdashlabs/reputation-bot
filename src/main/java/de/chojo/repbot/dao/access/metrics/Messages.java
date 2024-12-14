/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.snapshots.statistics.CountStatistics;
import de.chojo.repbot.dao.snapshots.statistics.CountsStatistic;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;

import static de.chojo.sadu.queries.api.call.Call.call;

/**
 * Provides methods to retrieve and save message metrics.
 */
public class Messages {
    /**
     * Constructs a Messages object.
     */
    public Messages() {

    }

    /**
     * Increments the message count for the current hour.
     */
    public void countMessage() {
        Query.query("""
                     INSERT INTO metrics_message_analyzed(hour, count) VALUES (date_trunc('hour', now()), 1)
                     ON CONFLICT(hour)
                         DO UPDATE SET count = metrics_message_analyzed.count + 1
                     """)
             .single()
             .insert();
    }

    /**
     * Retrieves the message counts for a specific hour.
     *
     * @param hour the hour number
     * @param count the number of records to retrieve
     * @return the counts statistic for the specified hour
     */
    public CountsStatistic hour(int hour, int count) {
        return get("metrics_message_analyzed", "hour", hour, count);
    }

    /**
     * Retrieves the message counts for a specific day.
     *
     * @param day the day number
     * @param count the number of records to retrieve
     * @return the counts statistic for the specified day
     */
    public CountsStatistic day(int day, int count) {
        return get("metrics_message_analyzed_day", "day", day, count);
    }

    /**
     * Retrieves the message counts for a specific week.
     *
     * @param week the week number
     * @param count the number of records to retrieve
     * @return the counts statistic for the specified week
     */
    public CountsStatistic week(int week, int count) {
        return get("metrics_message_analyzed_week", "week", week, count);
    }

    /**
     * Retrieves the message counts for a specific month.
     *
     * @param month the month number
     * @param count the number of records to retrieve
     * @return the counts statistic for the specified month
     */
    public CountsStatistic month(int month, int count) {
        return get("metrics_message_analyzed_month", "month", month, count);
    }

    /**
     * Retrieves the total message counts for a specific day.
     *
     * @param day the day number
     * @param count the number of records to retrieve
     * @return the counts statistic for the specified day
     */
    public CountsStatistic totalDay(int day, int count) {
        return get("metrics_messages_analyzed_total_day", "day", day, count);
    }

    /**
     * Retrieves the total message counts for a specific week.
     *
     * @param week the week number
     * @param count the number of records to retrieve
     * @return the counts statistic for the specified week
     */
    public CountsStatistic totalWeek(int week, int count) {
        return get("metrics_messages_analyzed_total_week", "week", week, count);
    }

    /**
     * Retrieves the total message counts for a specific month.
     *
     * @param month the month number
     * @param count the number of records to retrieve
     * @return the counts statistic for the specified month
     */
    public CountsStatistic totalMonth(int month, int count) {
        return get("metrics_messages_analyzed_total_month", "month", month, count);
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
                            WHERE %s <= date_trunc(?, now()) - ?::INTERVAL
                            ORDER BY %s DESC
                            LIMIT ?
                            """, timeframe, table, timeframe, timeframe)
                    .single(call().bind(timeframe).bind(offset + " " + timeframe).bind(count))
                    .map(rs -> CountStatistics.build(rs, timeframe))
                    .allResults()
                    .map(CountsStatistic::new);
    }
}
