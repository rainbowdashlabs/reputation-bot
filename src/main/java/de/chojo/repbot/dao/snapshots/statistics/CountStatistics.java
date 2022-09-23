package de.chojo.repbot.dao.snapshots.statistics;

import de.chojo.sadu.wrapper.util.Row;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.LocalDateTime;

public record CountStatistics(LocalDateTime date, int count) implements Comparable<CountStatistics> {

    public static CountStatistics build(Row rs, String dateKey) throws SQLException {
        return build(rs, "count", dateKey);
    }

    public static CountStatistics build(Row rs, String countKey, String dateKey) throws SQLException {
        return new CountStatistics(rs.getTimestamp(dateKey).toLocalDateTime(),
                rs.getInt(countKey));
    }

    @Override
    public int compareTo(@NotNull CountStatistics o) {
        return date().compareTo(o.date());
    }
}
