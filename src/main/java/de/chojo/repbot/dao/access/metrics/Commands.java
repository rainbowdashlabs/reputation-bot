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

/**
 * Data access object for handling command metrics.
 */
public class Commands {
    /**
     * Constructs a new Commands instance.
     */
    public Commands() {

    }

    /**
     * Logs a command execution.
     *
     * @param command the command to log
     */
    public void logCommand(String command) {
        Query.query("""
                     INSERT INTO metrics_commands(day, command) VALUES (now()::DATE, ?)
                     ON CONFLICT(day,command)
                         DO UPDATE SET count = metrics_commands.count + 1
                     """)
             .single(call().bind(command))
             .insert();
    }

    /**
     * Retrieves command statistics for a specific week.
     *
     * @param week the week number
     * @return the command statistics for the specified week
     */
    public CommandsStatistic week(int week) {
        return get("metrics_commands_week", "week", week);
    }

    /**
     * Retrieves command statistics for a specific month.
     *
     * @param month the month number
     * @return the command statistics for the specified month
     */
    public CommandsStatistic month(int month) {
        return get("metrics_commands_month", "month", month);
    }

    /**
     * Retrieves command execution counts for a specific week.
     *
     * @param week  the week number
     * @param count the number of entries to retrieve
     * @return the command execution counts for the specified week
     */
    public CountsStatistic week(int week, int count) {
        return get("metrics_commands_executed_week", "week", week, count);
    }

    /**
     * Retrieves command execution counts for a specific month.
     *
     * @param month the month number
     * @param count the number of entries to retrieve
     * @return the command execution counts for the specified month
     */
    public CountsStatistic month(int month, int count) {
        return get("metrics_commands_executed_month", "month", month, count);
    }

    /**
     * Retrieves command execution counts from the database.
     *
     * @param table     the table name
     * @param timeframe the timeframe (week or month)
     * @param offset    the offset value
     * @param count     the number of entries to retrieve
     * @return the command execution counts
     */
    private CountsStatistic get(String table, String timeframe, int offset, int count) {
        return Query.query("""
                        SELECT %s,
                            count
                        FROM %s
                        WHERE %s <= DATE_TRUNC(?, NOW())::date - ?::interval
                        ORDER BY %s DESC
                        LIMIT ?
                        """, timeframe, table, timeframe, timeframe)
                .single(call().bind(timeframe).bind(offset + " " + timeframe).bind(count))
                .map(rs -> CountStatistics.build(rs, timeframe))
                .allResults()
                .map(CountsStatistic::new);
    }

    /**
     * Retrieves command statistics from the database.
     *
     * @param table     the table name
     * @param timeframe the timeframe (week or month)
     * @param offset    the offset value
     * @return the command statistics
     */
    private CommandsStatistic get(String table, String timeframe, int offset) {
        return Query
                .query("""
                        SELECT %s,
                            command,
                            count
                        FROM %s
                        WHERE %s = DATE_TRUNC('%s', NOW())::date - ?::interval
                        """, timeframe, table, timeframe, timeframe)
                .single(call().bind(offset + " " + timeframe))
                .map(rs -> CommandStatistic.build(rs, timeframe))
                .allResults()
                .map(this::mapStatistics);
    }

    /**
     * Maps a list of command statistics to a CommandsStatistic object.
     *
     * @param commandStatistics the list of command statistics
     * @return the mapped CommandsStatistic object
     */
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
