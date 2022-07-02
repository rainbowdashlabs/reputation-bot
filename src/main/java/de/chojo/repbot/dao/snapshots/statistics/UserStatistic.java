package de.chojo.repbot.dao.snapshots.statistics;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public record UserStatistic(LocalDate date, int donors, int receivers, int total) implements Comparable<UserStatistic> {
    public static UserStatistic build(ResultSet rs, String dateKey) throws SQLException {
        return new UserStatistic(rs.getDate(dateKey).toLocalDate(),
                rs.getInt("donor_count"), rs.getInt("receiver_count"), rs.getInt("total_count"));
    }

    @Override
    public int compareTo(@NotNull UserStatistic o) {
        return date().compareTo(o.date());
    }
}
