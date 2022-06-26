package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.statistic.element.DataStatistic;
import de.chojo.repbot.util.LogNotify;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class Statistic extends QueryFactoryHolder {
    public Statistic(QueryFactoryHolder factoryHolder) {
        super(factoryHolder);
    }

    public Optional<DataStatistic> getStatistic() {
        return builder(DataStatistic.class).queryWithoutParams("""
                        SELECT
                        	guilds,
                        	active_guilds,
                        	active_channel,
                        	channel,
                        	total_reputation,
                        	today_reputation,
                        	weekly_reputation,
                        	weekly_avg_reputation
                        FROM
                        	data_statistics;
                        """).readRow(rs -> new DataStatistic(
                        rs.getInt("guilds"),
                        rs.getInt("active_guilds"),
                        rs.getInt("active_channel"),
                        rs.getInt("channel"),
                        rs.getInt("total_reputation"),
                        rs.getInt("today_reputation"),
                        rs.getInt("weekly_reputation"),
                        rs.getInt("weekly_avg_reputation")))
                .firstSync();
    }

    public void refreshStatistics() {
        builder().queryWithoutParams("REFRESH MATERIALIZED VIEW data_statistics").update().executeSync();
    }
}