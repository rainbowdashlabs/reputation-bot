package de.chojo.repbot.dao.access;

import de.chojo.sqlutil.base.QueryFactoryHolder;

import javax.sql.DataSource;

public class Migration extends QueryFactoryHolder {

    public Migration(DataSource dataSource) {
        super(dataSource);
    }

    public int getActiveMigrations(int days) {
        return builder(Integer.class).query("""
                        SELECT COUNT(1) FROM migrations WHERE prompted > NOW() - ?::interval
                        """)
                .paramsBuilder(stmt -> stmt.setString(days + "days"))
                .readRow(rs -> rs.getInt("count"))
                .firstSync().get();
    }
}
