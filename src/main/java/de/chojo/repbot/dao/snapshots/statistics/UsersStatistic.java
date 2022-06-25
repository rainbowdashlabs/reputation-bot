package de.chojo.repbot.dao.snapshots.statistics;

import de.chojo.repbot.util.TimeFormatter;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.AxesChartStyler;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.util.List;

public record UsersStatistic(List<UserStatistic> stats) implements ChartProvider {

    @Override
    public byte[] getChart(String title) {
        var categorySeries = new CategoryChartBuilder().width(1200).height(600)
                .title(title)
                .xAxisTitle("Date")
                .yAxisTitle("Counts")
                .theme(Styler.ChartTheme.Matlab)
                .build();

        var styler = categorySeries.getStyler();
        styler.setXAxisLabelRotation(20);
        styler.setXAxisLabelAlignmentVertical(AxesChartStyler.TextAlignment.Right);
        styler.setXAxisLabelAlignment(AxesChartStyler.TextAlignment.Right);

        var sorted = stats.stream().sorted().toList();

        categorySeries.addSeries("Donors",
                        sorted.stream().map(s -> TimeFormatter.date(s.date())).toList(),
                        sorted.stream().map(UserStatistic::donors).map(Double::valueOf).toList())
                .setMarker(SeriesMarkers.NONE)
                .setLabel("Donors");

        categorySeries.addSeries("Receivers",
                        sorted.stream().map(s -> TimeFormatter.date(s.date())).toList(),
                        sorted.stream().map(UserStatistic::receivers).map(Double::valueOf).toList())
                .setMarker(SeriesMarkers.NONE)
                .setLabel("Receivers");

        categorySeries.addSeries("Total",
                        sorted.stream().map(s -> TimeFormatter.date(s.date())).toList(),
                        sorted.stream().map(UserStatistic::total).map(Double::valueOf).toList())
                .setMarker(SeriesMarkers.NONE)
                .setLabel("Total");

        try {
            return BitmapEncoder.getBitmapBytes(categorySeries, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
