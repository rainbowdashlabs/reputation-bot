package de.chojo.repbot.dao.snapshots.statistics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public interface ChartProvider {
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    default String date(LocalDate date) {
        return DATE_FORMATTER.format(date);
    }

    default String date(LocalDateTime date) {
        return DATE_FORMATTER.format(date);
    }

    default String dateTime(LocalDateTime dateTime) {
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    default String time(LocalTime time) {
        return TIME_FORMATTER.format(time);
    }

    default String time(LocalDateTime time) {
        return TIME_FORMATTER.format(time);
    }

    byte[] getChart();
}
