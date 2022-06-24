package de.chojo.repbot.dao.snapshots.statistics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public record CountStatistics(LocalDate date, int count) implements ChartProvider{

    public static CountStatistics build(ResultSet rs, String dateKey) throws SQLException {
        return new CountStatistics(rs.getDate(dateKey).toLocalDate(),
                rs.getInt("count"));
    }

    @Override
    public byte[] getChart() {
        return new byte[0];
    }
}
