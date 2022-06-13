package de.chojo.repbot.dao.snapshots.statistics;

import java.time.LocalDate;
import java.util.List;

public record CommandsStatistic(LocalDate date, List<CommandStatistic> commands) {
}
