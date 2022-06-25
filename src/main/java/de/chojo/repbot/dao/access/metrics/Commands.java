package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.dao.snapshots.statistics.CommandStatistic;
import de.chojo.repbot.dao.snapshots.statistics.CommandsStatistic;
import de.chojo.sqlutil.base.QueryFactoryHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Commands extends QueryFactoryHolder {
    public Commands(QueryFactoryHolder factoryHolder) {
        super(factoryHolder);
    }

    public void logCommand(String command) {
        builder().query("""
                        INSERT INTO metrics_commands(day, command) VALUES (NOW()::date, ?)
                        ON CONFLICT(day,command)
                            DO UPDATE SET count = metrics_commands.count + 1
                        """)
                .paramsBuilder(stmt -> stmt.setString(command))
                .insert()
                .execute();
    }

    public CompletableFuture<CommandsStatistic> week(int week) {
        return get("metrics_commands_week", "week", week);
    }

    public CompletableFuture<CommandsStatistic> month(int month) {
        return get("metrics_commands_month", "month", month);
    }

    private CompletableFuture<CommandsStatistic> get(String table, String timeframe, int offset) {
        return builder(CommandStatistic.class).query("""
                        SELECT %s,
                            command,
                            count
                        FROM %s
                        WHERE %s = DATE_TRUNC('%s', NOW())::date - ?::INTERVAL
                        """, timeframe, table, timeframe,timeframe)
                .paramsBuilder(stmt -> stmt.setString(offset + " " + timeframe))
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
