package de.chojo.repbot.dao.snapshots.statistics;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.style.AxesChartStyler;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.util.List;

public record DowsStatistic(List<DowStatistics> stats) implements ChartProvider {

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
        styler.setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
        styler.setXAxisLabelAlignmentVertical(AxesChartStyler.TextAlignment.Right);
        styler.setXAxisLabelAlignment(AxesChartStyler.TextAlignment.Right);
        styler.setLegendVisible(false);

        var sorted = stats.stream().sorted().toList();

        categorySeries.addSeries("Counts",
                        sorted.stream().map(DowStatistics::dowAsString).toList(),
                        sorted.stream().map(DowStatistics::count).toList())
                .setMarker(SeriesMarkers.NONE)
                .setLabel("Counts");

        try {
            return BitmapEncoder.getBitmapBytes(categorySeries, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
