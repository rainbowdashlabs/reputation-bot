/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.statistics;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.AxesChartStyler;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

/**
 * Record for storing user statistics and providing chart generation.
 *
 * @param stats the list of user statistics
 */
public record UsersStatistic(List<UserStatistic> stats) implements ChartProvider {

    /**
     * Generates a chart image based on the user statistics.
     *
     * @param title the title of the chart
     * @return a byte array representing the chart image in PNG format
     */
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

    /**
     * Converts a LocalDate to a Date object.
     *
     * @param date the LocalDate to convert
     * @return the corresponding Date object
     */
    private Date toDate(LocalDate date) {
        return new Date(date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000);
    }
}
