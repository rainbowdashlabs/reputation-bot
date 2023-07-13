/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.snapshots.statistics.CommandStatistic;
import de.chojo.repbot.dao.snapshots.statistics.CommandsStatistic;
import de.chojo.repbot.dao.snapshots.statistics.CountStatistics;
import de.chojo.repbot.dao.snapshots.statistics.CountsStatistic;
import de.chojo.sadu.base.QueryFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Commands extends QueryFactory {
    public Commands(QueryFactory factoryHolder) {
        super(factoryHolder);
    }

    public void logCommand(String command) {
        builder()
                .query("""
                       INSERT INTO metrics_commands(day, command) VALUES (NOW()::date, ?)
                       ON CONFLICT(day,command)
                           DO UPDATE SET count = metrics_commands.count + 1
                       """)
                .parameter(stmt -> stmt.setString(command))
                .insert()
                .send();
    }

    public CompletableFuture<CommandsStatistic> week(int week) {
        return get("metrics_commands_week", "week", week);
    }

    public CompletableFuture<CommandsStatistic> month(int month) {
        return get("metrics_commands_month", "month", month);
    }

    public CompletableFuture<CountsStatistic> week(int week, int count) {
        return get("metrics_commands_executed_week", "week", week, count);
    }

    public CompletableFuture<CountsStatistic> month(int month, int count) {
        return get("metrics_commands_executed_month", "month", month, count);
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

    private CompletableFuture<CommandsStatistic> get(String table, String timeframe, int offset) {
        return builder(CommandStatistic.class)
                .query("""
                       SELECT %s,
                           command,
                           count
                       FROM %s
                       WHERE %s = DATE_TRUNC('%s', NOW())::date - ?::interval
                       """, timeframe, table, timeframe, timeframe)
                .parameter(stmt -> stmt.setString(offset + " " + timeframe))
                .readRow(rs -> CommandStatistic.build(rs, timeframe))
                .all()
                .thenApply(this::mapStatistics);
    }

    private CommandsStatistic mapStatistics(List<CommandStatistic> commandStatistics) {
        return commandStatistics.stream()
                                .collect(Collectors.groupingBy(CommandStatistic::date))
                                .entrySet()
                                .stream()
                                .map(entry -> new CommandsStatistic(entry.getKey(), entry.getValue()))
                                .limit(1)
                                .findFirst()
                                .orElse(new CommandsStatistic(LocalDate.MIN, Collections.emptyList()));
    }
}
