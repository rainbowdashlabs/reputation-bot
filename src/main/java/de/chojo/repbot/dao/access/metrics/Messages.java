/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.snapshots.statistics.CountStatistics;
import de.chojo.repbot.dao.snapshots.statistics.CountsStatistic;
import de.chojo.sadu.queries.api.query.Query;

import static de.chojo.sadu.queries.api.call.Call.call;

public class Messages {
    public Messages() {

    }

    public void countMessage() {
        Query.query("""
                     INSERT INTO metrics_message_analyzed(hour, count) VALUES (date_trunc('hour', now()), 1)
                     ON CONFLICT(hour)
                         DO UPDATE SET count = metrics_message_analyzed.count + 1
                     """)
             .single()
             .insert();
    }

    public CountsStatistic hour(int hour, int count) {
        return get("metrics_message_analyzed", "hour", hour, count);
    }

    public CountsStatistic day(int day, int count) {
        return get("metrics_message_analyzed_day", "day", day, count);
    }

    public CountsStatistic week(int week, int count) {
        return get("metrics_message_analyzed_week", "week", week, count);
    }

    public CountsStatistic month(int month, int count) {
        return get("metrics_message_analyzed_month", "month", month, count);
    }

    public CountsStatistic totalDay(int day, int count) {
        return get("metrics_messages_analyzed_total_day", "day", day, count);
    }

    public CountsStatistic totalWeek(int week, int count) {
        return get("metrics_messages_analyzed_total_week", "week", week, count);
    }

    public CountsStatistic totalMonth(int month, int count) {
        return get("metrics_messages_analyzed_total_month", "month", month, count);
    }

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
