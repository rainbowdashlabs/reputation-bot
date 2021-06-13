package de.chojo.repbot.data.util;

import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class DbUtil {
    private static final Logger log = getLogger(DbUtil.class);

    public static void logSQLError(String message, SQLException ex) {
        log.error("{}\nMessage: {}\nCode: {}\nState: {}",
                message, ex.getMessage(), ex.getErrorCode(), ex.getSQLState(), ex);
    }

    public static <T> Set<T> arrayToSet(ResultSet resultSet, String column) throws SQLException {
        T[] objects = arrayToArray(resultSet, column);
        return objects == null ? new HashSet<>() : Set.of();
    }

    public static <T> List<T> arrayToList(ResultSet resultSet, String column) throws SQLException {
        T[] objects = arrayToArray(resultSet, column);
        return objects == null ? new ArrayList<>() : List.of(objects);
    }

    @Nullable
    public static <T> T[] arrayToArray(ResultSet resultSet, String column) throws SQLException {
        return arrayToArray(resultSet, column, null);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T[] arrayToArray(ResultSet resultSet, String column, @Nullable T[] def) throws SQLException {
        var array = resultSet.getArray(column);
        return array == null ? def : (T[]) resultSet.getArray(column).getArray();
    }
}
