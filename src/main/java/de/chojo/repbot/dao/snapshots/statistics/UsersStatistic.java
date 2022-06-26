package de.chojo.repbot.dao.snapshots.statistics;

import de.chojo.repbot.util.TimeFormatter;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.AxesChartStyler;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

public record UsersStatistic(List<UserStatistic> stats) implements ChartProvider {

    @Override
    public byte[] getChart(String title) {
        var categorySeries = new XYChartBuilder().width(1200).height(600)
                .title(title)
                .xAxisTitle("Date")
                .yAxisTitle("Counts")
                .theme(Styler.ChartTheme.Matlab)
                .build();

        var styler = categorySeries.getStyler();
        styler.setXAxisLabelRotation(20);
        styler.setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        styler.setXAxisLabelAlignmentVertical(AxesChartStyler.TextAlignment.Right);
        styler.setXAxisLabelAlignment(AxesChartStyler.TextAlignment.Right);

        var sorted = stats.stream().sorted().toList();

        categorySeries.addSeries("Total",
                        sorted.stream().map(s -> toDate(s.date())).toList(),
                        sorted.stream().map(UserStatistic::total).map(Double::valueOf).toList())
                .setMarker(SeriesMarkers.NONE)
                .setLabel("Total");

        categorySeries.addSeries("Donors",
                        sorted.stream().map(s -> toDate(s.date())).toList(),
                        sorted.stream().map(UserStatistic::donors).map(Double::valueOf).toList())
                .setMarker(SeriesMarkers.NONE)
                .setLabel("Donors");

        categorySeries.addSeries("Receivers",
                        sorted.stream().map(s -> toDate(s.date())).toList(),
                        sorted.stream().map(UserStatistic::receivers).map(Double::valueOf).toList())
                .setMarker(SeriesMarkers.NONE)
                .setLabel("Receivers");


        try {
            return BitmapEncoder.getBitmapBytes(categorySeries, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Date toDate(LocalDate date){
        return new Date(date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) *1000);
    }
}
