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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

public record LabeledCountStatistic(Map<String, List<CountStatistics>> stats) implements ChartProvider {

    @Override
    public byte[] getChart(String title) {
        var categorySeries = new XYChartBuilder()
                .width(1200)
                .height(600)
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

        for (var entry : stats.entrySet()) {
            var sorted = entry.getValue().stream().sorted().toList();
            categorySeries
                    .addSeries(
                            entry.getKey(),
                            sorted.stream().map(s -> toDate(s.date())).toList(),
                            sorted.stream()
                                    .map(CountStatistics::count)
                                    .map(Double::valueOf)
                                    .toList())
                    .setMarker(SeriesMarkers.NONE)
                    .setLabel(entry.getKey());
        }

        try {
            return BitmapEncoder.getBitmapBytes(categorySeries, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Date toDate(LocalDateTime date) {
        return new Date(date.toEpochSecond(ZoneOffset.UTC) * 1000);
    }
}
