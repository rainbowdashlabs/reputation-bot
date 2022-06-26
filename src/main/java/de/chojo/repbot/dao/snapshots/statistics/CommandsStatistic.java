package de.chojo.repbot.dao.snapshots.statistics;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.AxesChartStyler;
import org.knowm.xchart.style.Styler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public record CommandsStatistic(LocalDate date, List<CommandStatistic> commands) implements ChartProvider {

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
