/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.statistics;

import de.chojo.sadu.wrapper.util.Row;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.LocalDate;

public record UserStatistic(LocalDate date, int donors, int receivers, int total) implements Comparable<UserStatistic> {
    public static UserStatistic build(Row rs, String dateKey) throws SQLException {
        return new UserStatistic(rs.getDate(dateKey).toLocalDate(),
                rs.getInt("donor_count"), rs.getInt("receiver_count"), rs.getInt("total_count"));
    }

    @Override
    public int compareTo(@NotNull UserStatistic o) {
        return date().compareTo(o.date());
    }
}
