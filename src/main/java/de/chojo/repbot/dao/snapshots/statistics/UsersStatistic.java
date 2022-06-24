package de.chojo.repbot.dao.snapshots.statistics;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

public record UsersStatistic(List<UserStatistic> stats) implements ChartProvider{

    @Override
    public byte[] getChart() {
        var categorySeries = new CategoryChartBuilder().width(800).height(600)
                .title("User Statistics")
                .xAxisTitle("Date")
                .yAxisTitle("Counts")
                .theme(Styler.ChartTheme.Matlab)
                .build();

        categorySeries.setCustomXAxisTickLabelsFormatter(r -> date(stats.get(r.intValue()).date()));
        categorySeries.addSeries("Donors",
                        IntStream.range(0, stats.size()).mapToObj(Double::valueOf).toList(),
                        stats.stream().map(UserStatistic::donors).map(Double::valueOf).toList())
                .setMarker(SeriesMarkers.NONE)
                .setLabel("Donors");

        categorySeries.addSeries("Receivers",
                        IntStream.range(0, stats.size()).mapToObj(Double::valueOf).toList(),
                        stats.stream().map(UserStatistic::receivers).map(Double::valueOf).toList())
                .setMarker(SeriesMarkers.NONE)
                .setLabel("Receivers");

        try {
            return BitmapEncoder.getBitmapBytes(categorySeries, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
