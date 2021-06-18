package de.chojo.repbot.data;

import de.chojo.repbot.statistic.element.DataStatistic;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;

import javax.sql.DataSource;
import java.util.Optional;

public class StatisticData extends QueryFactoryHolder {
    /**
     * Create a new StatisticData
     *
     * @param dataSource datasource
     */
    public StatisticData(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder().build());
    }

    public Optional<DataStatistic> getStatistic() {
        return builder(DataStatistic.class).queryWithoutParams("""
                SELECT
                	guilds,
                	channel,
                	total_reputation,
                	today_reputation,
                	weekly_reputation,
                	weekly_avg_reputation
                FROM
                	data_statistics;
                """).readRow(rs -> new DataStatistic(rs.getInt("guilds"), rs.getInt("channel"),
                rs.getInt("total_reputation"), rs.getInt("today_reputation"), rs.getInt("weekly_reputation"),
                rs.getInt("weeky_avg_reputation")))
        .firstSync();
    }
}
