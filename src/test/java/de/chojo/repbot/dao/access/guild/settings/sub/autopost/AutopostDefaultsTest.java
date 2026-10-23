/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.autopost;

import de.chojo.repbot.dataconsistency.TestSQL;
import de.chojo.repbot.web.pojo.settings.sub.AutopostPOJO;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.updater.SqlUpdater;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@Tag("database")
class AutopostDefaultsTest {

    @Test
    void firstSavedSettingKeepsTheDefaultsTheDashboardShows() throws IOException, SQLException {
        try (var container = TestSQL.createContainer("postgres", "postgres");
                var dataSource = DataSourceCreator.create(PostgreSql.get())
                        .configure(conf -> conf.port(container.getFirstMappedPort()))
                        .create()
                        .usingUsername("postgres")
                        .usingPassword("postgres")
                        .build()) {
            SqlUpdater.builder(dataSource, PostgreSql.get())
                    .setSchemas("repbot_schema")
                    .execute();
            try (var conn = dataSource.getConnection();
                    var stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO repbot_schema.guilds(guild_id) VALUES (1)");
                stmt.execute("INSERT INTO repbot_schema.autopost(guild_id, active) VALUES (1, TRUE)");
                var rs = stmt.executeQuery(
                        "SELECT refresh_type, refresh_interval FROM repbot_schema.autopost WHERE guild_id = 1");
                Assertions.assertTrue(rs.next());
                var defaults = new AutopostPOJO();
                Assertions.assertEquals(defaults.refreshType().name(), rs.getString("refresh_type"));
                Assertions.assertEquals(defaults.refreshInterval().name(), rs.getString("refresh_interval"));
            }
        }
    }
}
