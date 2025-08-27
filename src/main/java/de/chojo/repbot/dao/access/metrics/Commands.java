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
import de.chojo.sadu.queries.api.query.Query;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;

public class Commands {
    public Commands() {

    }

    public void logCommand(String command) {
        Query.query("""
                     INSERT INTO metrics_commands(day, command) VALUES (now()::DATE, ?)
                     ON CONFLICT(day,command)
                         DO UPDATE SET count = metrics_commands.count + 1
                     """)
             .single(call().bind(command))
             .insert();
    }

    public CommandsStatistic week(int week) {
        return get("metrics_commands_week", "week", week);
    }

    public CommandsStatistic month(int month) {
        return get("metrics_commands_month", "month", month);
    }

    public CountsStatistic week(int week, int count) {
        return get("metrics_commands_executed_week", "week", week, count);
    }

    public CountsStatistic month(int month, int count) {
        return get("metrics_commands_executed_month", "month", month, count);
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

    private CommandsStatistic get(String table, String timeframe, int offset) {
        return Query
                .query("""
                        SELECT %s,
                            command,
                            count
                        FROM %s
                        WHERE %s = date_trunc('%s', now())::DATE - ?::INTERVAL
                        """, timeframe, table, timeframe, timeframe)
                .single(call().bind(offset + " " + timeframe))
                .map(rs -> CommandStatistic.build(rs, timeframe))
                .allResults()
                .map(this::mapStatistics);
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
