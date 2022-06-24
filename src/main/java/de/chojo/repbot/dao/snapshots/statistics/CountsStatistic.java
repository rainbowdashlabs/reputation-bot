package de.chojo.repbot.dao.snapshots.statistics;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

public record CountsStatistic(List<CountStatistics> stats) implements ChartProvider {


    public CountStatistics get(int index) {
        if (stats.isEmpty()) {
            return new CountStatistics(LocalDate.MIN, 0);
        }
        return stats.get(index);
    }

    @Override
    public byte[] getChart() {
        var categorySeries = new CategoryChartBuilder().width(800).height(600)
                .title("User Statistics")
                .xAxisTitle("Date")
                .yAxisTitle("Count")
                .theme(Styler.ChartTheme.Matlab)
                .build();
        categorySeries.setCustomXAxisTickLabelsFormatter(r -> date(stats.get(r.intValue()).date()));
        categorySeries.addSeries("Counts",
                        IntStream.range(0, stats.size()).mapToObj(Double::valueOf).toList(),
                        stats.stream().map(CountStatistics::count).map(Double::valueOf).toList())
                .setMarker(SeriesMarkers.NONE)
                .setLabel("Counts");

        try {
            return BitmapEncoder.getBitmapBytes(categorySeries, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
