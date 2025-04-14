/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.statistics;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.AxesChartStyler;
import org.knowm.xchart.style.Styler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents the statistics of commands executed on a specific date.
 *
 * @param date     the date of the statistics
 * @param commands the list of command statistics
 */
public record CommandsStatistic(LocalDate date, List<CommandStatistic> commands) implements ChartProvider {

    /**
     * Generates a chart representing the command statistics.
     *
     * @param title the title of the chart
     * @return a byte array containing the chart image in PNG format
     */
    @Override
    public byte[] getChart(String title) {
        var categorySeries = new CategoryChartBuilder().width(1200).height(600)
                                                       .title(title)
                                                       .xAxisTitle("Command")
                                                       .yAxisTitle("Count")
                                                       .theme(Styler.ChartTheme.Matlab)
                                                       .build();
        var style = categorySeries.getStyler();
        style.setXAxisLabelAlignment(AxesChartStyler.TextAlignment.Centre);
        style.setXAxisLabelAlignmentVertical(AxesChartStyler.TextAlignment.Right);
        style.setLegendVisible(false);
        style.setAxisTickPadding(10);
        style.setXAxisTitleVisible(false);

        var xData = commands.stream().map(CommandStatistic::command).toList();
        var yData = commands.stream().map(CommandStatistic::count).toList();
        categorySeries.addSeries("Command",
                              xData,
                              yData)
                      .setShowInLegend(false)
                      .setLabel("command");

        try {
            return BitmapEncoder.getBitmapBytes(categorySeries, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
