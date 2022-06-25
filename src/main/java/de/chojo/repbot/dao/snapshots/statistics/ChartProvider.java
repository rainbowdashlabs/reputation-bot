package de.chojo.repbot.dao.snapshots.statistics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public interface ChartProvider {

    byte[] getChart(String title);
}
