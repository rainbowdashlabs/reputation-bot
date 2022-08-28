package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.CountStatistics;
import de.chojo.repbot.dao.snapshots.statistics.LabeledCountStatistic;
import de.chojo.repbot.dao.snapshots.statistics.builder.LabeledCountStatisticBuilder;
import de.chojo.sadu.base.QueryFactory;

import java.util.concurrent.CompletableFuture;

public class Service extends QueryFactory {
    public Service(Metrics metrics) {
        super(metrics);
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
        builder()
                .queryWithoutParams("""
                                    INSERT INTO metrics_handled_interactions(hour, %s) VALUES (DATE_TRUNC('hour', NOW()), 1)
                                    ON CONFLICT(hour)
                                        DO UPDATE SET %s = metrics_handled_interactions.%s + 1
                                    """, type, type, type)
                .insert()
                .send();
    }

    public CompletableFuture<LabeledCountStatistic> hour(int hour, int count) {
        return get("metrics_handled_interactions", "hour", hour, count);
    }

    public CompletableFuture<LabeledCountStatistic> day(int day, int count) {
        return get("metrics_handled_interactions_day", "day", day, count);
    }

    public CompletableFuture<LabeledCountStatistic> week(int week, int count) {
        return get("metrics_handled_interactions_week", "week", week, count);
    }

    public CompletableFuture<LabeledCountStatistic> month(int month, int count) {
        return get("metrics_handled_interactions_month", "month", month, count);
    }

    private CompletableFuture<LabeledCountStatistic> get(String table, String timeframe, int offset, int count) {
        var builder = new LabeledCountStatisticBuilder();
        return builder(LabeledCountStatisticBuilder.class)
                .query("""
                       SELECT %s,
                           count,
                           failed,
                           success
                       FROM %s
                       WHERE %s <= DATE_TRUNC(?, NOW()) - ?::interval
                       ORDER BY %s DESC
                       LIMIT ?
                       """, timeframe, table, timeframe, timeframe)
                .parameter(stmt -> stmt.setString(timeframe)
                                       .setString(offset + " " + timeframe)
                                       .setInt(count))
                .readRow(rs -> builder.add("count", CountStatistics.build(rs, "count", timeframe))
                                      .add("success", CountStatistics.build(rs, "success", timeframe))
                                      .add("failed", CountStatistics.build(rs, "failed", timeframe))
                )
                .all()
                .thenApply(r -> builder.build());
    }
}
