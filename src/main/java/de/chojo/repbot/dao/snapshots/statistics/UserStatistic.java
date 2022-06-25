package de.chojo.repbot.dao.snapshots.statistics;

import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

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
