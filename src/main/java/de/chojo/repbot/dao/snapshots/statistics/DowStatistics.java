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

public record DowStatistics(LocalDate date, int dow, int count) implements Comparable<DowStatistics> {

    public static DowStatistics build(Row rs, String dateKey) throws SQLException {
        return new DowStatistics(rs.getDate(dateKey).toLocalDate(), rs.getInt("dow"),
                rs.getInt("count"));
    }

    @Override
    public int compareTo(@NotNull DowStatistics o) {
        return date().compareTo(o.date());
    }

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
