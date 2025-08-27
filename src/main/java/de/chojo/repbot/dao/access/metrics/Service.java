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

public class Service {
    public Service() {
        super();
    }

    public void successfulInteraction() {
        logInteraction("success");
    }

    public void failedInteraction() {
        logInteraction("failed");
    }

    public void countInteraction() {
        logInteraction("count");
    }

    private void logInteraction(String type) {
        Query.query("""
                     INSERT INTO metrics_handled_interactions(hour, %s) VALUES (date_trunc('hour', now()), 1)
                     ON CONFLICT(hour)
                         DO UPDATE SET %s = metrics_handled_interactions.%s + 1
                     """, type, type, type)
             .single()
             .insert();
    }

    public LabeledCountStatistic hour(int hour, int count) {
        return get("metrics_handled_interactions", "hour", hour, count);
    }

    public LabeledCountStatistic day(int day, int count) {
        return get("metrics_handled_interactions_day", "day", day, count);
    }

    public LabeledCountStatistic week(int week, int count) {
        return get("metrics_handled_interactions_week", "week", week, count);
    }

    public LabeledCountStatistic month(int month, int count) {
        return get("metrics_handled_interactions_month", "month", month, count);
    }

    private LabeledCountStatistic get(String table, String timeframe, int offset, int count) {
        var builder = new LabeledCountStatisticBuilder();
        return Query.query("""
                            SELECT %s,
                                count,
                                failed,
                                success
                            FROM %s
                            WHERE %s <= date_trunc(?, now()) - ?::INTERVAL
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
