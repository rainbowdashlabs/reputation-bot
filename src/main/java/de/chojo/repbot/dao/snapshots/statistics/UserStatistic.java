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
 * Record representing user statistics.
 *
 * @param date the date of the statistics
 * @param donors the number of donors
 * @param receivers the number of receivers
 * @param total the total count
 */
public record UserStatistic(LocalDate date, int donors, int receivers, int total) implements Comparable<UserStatistic> {

    /**
     * Builds a UserStatistic instance from a database row.
     *
     * @param rs the database row
     * @param dateKey the key for the date column
     * @return the created UserStatistic instance
     * @throws SQLException if a database access error occurs
     */
    public static UserStatistic build(Row rs, String dateKey) throws SQLException {
        return new UserStatistic(rs.getDate(dateKey).toLocalDate(),
                rs.getInt("donor_count"), rs.getInt("receiver_count"), rs.getInt("total_count"));
    }

    /**
     * Compares this UserStatistic to another based on the date.
     *
     * @param o the other UserStatistic to compare to
     * @return a negative integer, zero, or a positive integer as this UserStatistic is less than, equal to, or greater than the specified UserStatistic
     */
    @Override
    public int compareTo(@NotNull UserStatistic o) {
        return date().compareTo(o.date());
    }
}
