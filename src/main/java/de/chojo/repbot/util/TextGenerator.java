package de.chojo.repbot.util;

import org.apache.commons.lang3.StringUtils;

public final class TextGenerator {
    private static final String EMPTY = "░";
    private static final String FULL = "▓";

    private TextGenerator() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static String progressBar(double percent, int tiles) {
        var progressBar = StringUtils.repeat(FULL, (int) Math.round(percent * tiles));
        return StringUtils.rightPad(progressBar, tiles, EMPTY);
    }
}
