package de.chojo.repbot.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class TimeFormatter {
    private TimeFormatter() {
        throw new UnsupportedOperationException("This is a utility class.");
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
}
