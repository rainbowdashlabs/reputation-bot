package de.chojo.repbot.dao.snapshots.statistics;

import de.chojo.sadu.wrapper.util.Row;

import java.sql.SQLException;
import java.time.LocalDate;

public record CommandStatistic(LocalDate date, String command, int count) {

    public static CommandStatistic build(Row rs, String dateKey) throws SQLException {
        return new CommandStatistic(rs.getDate(dateKey).toLocalDate(),
                rs.getString("command"),
                rs.getInt("count"));
    }
}
