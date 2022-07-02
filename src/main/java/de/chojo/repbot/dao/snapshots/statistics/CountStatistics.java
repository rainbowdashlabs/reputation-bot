package de.chojo.repbot.dao.snapshots.statistics;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public record CountStatistics(LocalDate date, int count) implements Comparable<CountStatistics> {

    public static CountStatistics build(ResultSet rs, String dateKey) throws SQLException {
        return new CountStatistics(rs.getDate(dateKey).toLocalDate(),
                rs.getInt("count"));
    }

    @Override
    public int compareTo(@NotNull CountStatistics o) {
        return date().compareTo(o.date());
    }
}
