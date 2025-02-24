/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.statistics;

import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Record class representing a command statistic.
 *
 * @param date    the date of the statistic
 * @param command the command name
 * @param count   the count of the command executions
 */
public record CommandStatistic(LocalDate date, String command, int count) {

    /**
     * Builds a CommandStatistic instance from a database row.
     *
     * @param rs      the database row
     * @param dateKey the key for the date column
     * @return a CommandStatistic instance
     * @throws SQLException if a database access error occurs
     */
    public static CommandStatistic build(Row rs, String dateKey) throws SQLException {
        return new CommandStatistic(rs.getDate(dateKey).toLocalDate(),
                rs.getString("command"),
                rs.getInt("count"));
    }
}
