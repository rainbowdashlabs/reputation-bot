/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.statistics;

import de.chojo.sadu.mapper.wrapper.Row;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Represents the statistics for a specific day of the week.
 *
 * @param date  the date of the statistics
 * @param dow   the day of the week (1 for Monday, 2 for Tuesday, etc.)
 * @param count the count of occurrences on that day
 */
public record DowStatistics(LocalDate date, int dow, int count) implements Comparable<DowStatistics> {

    /**
     * Builds a DowStatistics instance from a database row.
     *
     * @param rs      the database row
     * @param dateKey the key for the date column
     * @return a new DowStatistics instance
     * @throws SQLException if a database access error occurs
     */
    public static DowStatistics build(Row rs, String dateKey) throws SQLException {
        return new DowStatistics(rs.getDate(dateKey).toLocalDate(), rs.getInt("dow"),
                rs.getInt("count"));
    }

    /**
     * Compares this DowStatistics instance with another based on the date.
     *
     * @param o the other DowStatistics instance
     * @return a negative integer, zero, or a positive integer as this instance is less than, equal to, or greater than the specified object
     */
    @Override
    public int compareTo(@NotNull DowStatistics o) {
        return date().compareTo(o.date());
    }

    /**
     * Converts the day of the week to its string representation.
     *
     * @return the string representation of the day of the week
     */
    public String dowAsString() {
        return switch (dow()) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            case 6 -> "Saturday";
            case 7 -> "Sunday";
            default -> "";
        };
    }
}
