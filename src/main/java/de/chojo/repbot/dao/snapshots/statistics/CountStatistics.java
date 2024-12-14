/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.statistics;

import de.chojo.sadu.mapper.wrapper.Row;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Represents a count statistic with a date and count value.
 *
 * @param date  the date of the statistic
 * @param count the count value of the statistic
 */
public record CountStatistics(LocalDateTime date, int count) implements Comparable<CountStatistics> {

    /**
     * Builds a CountStatistics instance from a database row using the specified date key.
     *
     * @param rs      the database row
     * @param dateKey the key for the date column
     * @return a new CountStatistics instance
     * @throws SQLException if a database access error occurs
     */
    public static CountStatistics build(Row rs, String dateKey) throws SQLException {
        return build(rs, "count", dateKey);
    }

    /**
     * Builds a CountStatistics instance from a database row using the specified count and date keys.
     *
     * @param rs       the database row
     * @param countKey the key for the count column
     * @param dateKey  the key for the date column
     * @return a new CountStatistics instance
     * @throws SQLException if a database access error occurs
     */
    public static CountStatistics build(Row rs, String countKey, String dateKey) throws SQLException {
        return new CountStatistics(rs.getTimestamp(dateKey).toLocalDateTime(), rs.getInt(countKey));
    }

    /**
     * Compares this CountStatistics instance with another based on the date.
     *
     * @param o the other CountStatistics instance
     * @return a negative integer, zero, or a positive integer as this instance is less than, equal to, or greater than the specified instance
     */
    @Override
    public int compareTo(@NotNull CountStatistics o) {
        return date().compareTo(o.date());
    }
}
