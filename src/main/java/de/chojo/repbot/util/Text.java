package de.chojo.repbot.util;

import de.chojo.jdautil.wrapper.EventContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.PropertyKey;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Text {

    private static final String EMPTY = "░";
    private static final String FULL = "▓";

    private Text() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static String progressBar(double percent, int tiles) {
        var progressBar = StringUtils.repeat(FULL, (int) Math.round(percent * tiles));
        return StringUtils.rightPad(progressBar, tiles, EMPTY);
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static String date(LocalDate date) {
        return DATE_FORMATTER.format(date);
    }

    public static String month(LocalDate date) {
        return DATE_FORMATTER.format(date);
    }

    public static String date(LocalDateTime date) {
        return DATE_FORMATTER.format(date);
    }

    public static String dateTime(LocalDateTime dateTime) {
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    public static String time(LocalTime time) {
        return TIME_FORMATTER.format(time);
    }

    public static String time(LocalDateTime time) {
        return TIME_FORMATTER.format(time);
    }

    public static String getBooleanMessage(EventContext context, boolean value, String whenTrue, String whenFalse) {
        return context.localize(value ? whenTrue : whenFalse);
    }

    public static String getSetting(@PropertyKey(resourceBundle = "locale") String locale, Object object) {
        return String.format("$%s$: %s", locale, object);
    }

    public static String getSetting(@PropertyKey(resourceBundle = "locale") String locale, boolean enabled) {
        return String.format("$%s$: $%s$", locale, enabled ? "words.enabled" : "words.disabled");
    }
}
