/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.metrics;

import de.chojo.repbot.statistic.element.DataStatistic;
import de.chojo.sadu.queries.api.query.Query;

import java.util.Optional;

public class Statistic {
    public Statistic() {
        super();
    }

    public Optional<DataStatistic> getStatistic() {
        return Query.query("""
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
                            """)
                .single()
                .map(rs -> new DataStatistic(
                        rs.getInt("guilds"),
                        rs.getInt("active_guilds"),
                        rs.getInt("active_channel"),
                        rs.getInt("channel"),
                        rs.getInt("total_reputation"),
                        rs.getInt("today_reputation"),
                        rs.getInt("weekly_reputation"),
                        rs.getInt("weekly_avg_reputation")))
                .first();
    }

    public void refreshStatistics() {
        Query.query("REFRESH MATERIALIZED VIEW data_statistics").single().update();
    }
}
