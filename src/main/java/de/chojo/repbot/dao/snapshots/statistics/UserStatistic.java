package de.chojo.repbot.dao.snapshots.statistics;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public record UserStatistic(LocalDate date, int donors, int receivers) implements ChartProvider {
    private static final String[] labels = {"Donors", "Receivers"};

    public static UserStatistic build(ResultSet rs, String dateKey) throws SQLException {
        return new UserStatistic(rs.getDate(dateKey).toLocalDate(),
                rs.getInt("donor_count"), rs.getInt("receiver_count"));
    }


    @Override
    public byte[] getChart() {
        var categorySeries = new CategoryChartBuilder().width(800).height(600)
                .title("User statistic for " + date(date()))
                .xAxisTitle("Type")
                .yAxisTitle("Count")
                .theme(Styler.ChartTheme.Matlab)
                .build();
        categorySeries.setCustomXAxisTickLabelsFormatter(r -> labels[r.intValue()]);
        categorySeries.addSeries("Counts",
                        List.of(0, 1),
                        List.of(donors, receivers))
                .setMarker(SeriesMarkers.NONE)
                .setLabel("command");

        try {
            return BitmapEncoder.getBitmapBytes(categorySeries, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
