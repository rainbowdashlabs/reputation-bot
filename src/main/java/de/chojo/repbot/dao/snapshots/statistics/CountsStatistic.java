/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.statistics;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.AxesChartStyler;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

/**
 * Represents a statistic of counts over time and provides a method to generate a chart.
 *
 * @param stats the list of count statistics
 */
public record CountsStatistic(List<CountStatistics> stats) implements ChartProvider {

    /**
     * Gets the count statistics at the specified index.
     *
     * @param index the index of the count statistics
     * @return the count statistics at the specified index, or a default value if the list is empty
     */
    public CountStatistics get(int index) {
        if (stats.isEmpty()) {
            return new CountStatistics(LocalDateTime.MIN, 0);
        }
        return stats.get(index);
    }

    /**
     * Generates a chart representing the count statistics.
     *
     * @param title the title of the chart
     * @return a byte array representing the chart in PNG format
     */
    @Override
    public byte[] getChart(String title) {
        var categorySeries = new XYChartBuilder().width(1200).height(600)
                                                 .title(title)
                                                 .xAxisTitle("Date")
                                                 .yAxisTitle("Count")
                                                 .theme(Styler.ChartTheme.Matlab)
                                                 .build();

        var styler = categorySeries.getStyler();
        styler.setLegendVisible(false);
        styler.setXAxisLabelRotation(20);
        styler.setXAxisLabelAlignmentVertical(AxesChartStyler.TextAlignment.Right);
        styler.setXAxisLabelAlignment(AxesChartStyler.TextAlignment.Right);
        styler.setYAxisDecimalPattern("##,###");

        var sorted = stats.stream().sorted().toList();

        categorySeries.addSeries("Counts",
                              sorted.stream().map(countStatistics -> toDate(countStatistics.date())).toList(),
                              sorted.stream().map(CountStatistics::count).toList())
                      .setMarker(SeriesMarkers.NONE)
                      .setLabel("Counts");

        try {
            return BitmapEncoder.getBitmapBytes(categorySeries, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a LocalDateTime to a Date object.
     *
     * @param date the LocalDateTime to convert
     * @return the corresponding Date object
     */
    private Date toDate(LocalDateTime date) {
        return new Date(date.toEpochSecond(ZoneOffset.UTC) * 1000);
    }
}
