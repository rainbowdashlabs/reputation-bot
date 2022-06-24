package de.chojo.repbot.statistics;

import org.junit.jupiter.api.Test;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class GraphTest {


    @Test
    public void generateBar() {
        var cmds = new String[]{"first", "second", "third"};
        var id = new double[]{1, 2};
        var count = new double[]{4, 5};

        var categorySeries = new CategoryChartBuilder().width(800).height(600)
                .title("Commands")
                .xAxisTitle("Command")
                .yAxisTitle("Count")
                .theme(Styler.ChartTheme.Matlab)
                .build();
        categorySeries.setCustomXAxisTickLabelsFormatter(d -> cmds[d.intValue()]);
        categorySeries.addSeries("asd", new double[]{0, 1, 2}, new double[]{4, 8, 6})
                .setMarker(SeriesMarkers.NONE)
                .setLabel("command");

        try {
            var bitmapBytes = BitmapEncoder.getBitmapBytes(categorySeries, BitmapEncoder.BitmapFormat.PNG);
            Files.write(Path.of("asdf.png"), bitmapBytes, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
