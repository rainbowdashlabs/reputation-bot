package de.chojo.repbot.dao.snapshots.statistics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public record UserStatistic(LocalDate date, int donors, int receivers) {

    public static UserStatistic build(ResultSet rs, String dateKey) throws SQLException {
        return new UserStatistic(rs.getDate(dateKey).toLocalDate(),
                rs.getInt("donors"), rs.getInt("receivers"));
    }
}
