/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.provider;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.repbot.dataconsistency.TestSQL;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.postgresql.mapper.PostgresqlMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.updater.SqlUpdater;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.sql.SQLException;

@Tag("database")
class UserSessionRepositoryTest {
    private static GenericContainer<?> container;
    private static HikariDataSource dataSource;
    private final UserSessionRepository repository = new UserSessionRepository();

    @BeforeAll
    static void setUp() throws IOException, SQLException {
        container = TestSQL.createContainer("postgres", "postgres");
        dataSource = DataSourceCreator.create(PostgreSql.get())
                .configure(conf -> conf.port(container.getFirstMappedPort()))
                .create()
                .usingUsername("postgres")
                .usingPassword("postgres")
                .forSchema("repbot_schema")
                .build();
        SqlUpdater.builder(dataSource, PostgreSql.get())
                .setSchemas("repbot_schema")
                .execute();
        var registry = new RowMapperRegistry();
        registry.register(PostgresqlMapper.getDefaultMapper());
        QueryConfiguration.setDefault(QueryConfiguration.builder(dataSource)
                .setRowMapperRegistry(registry)
                .build());
    }

    @AfterAll
    static void tearDown() {
        dataSource.close();
        container.stop();
    }

    private static void lastUsedHoursAgo(String token, int hours) throws SQLException {
        try (var conn = dataSource.getConnection();
                var stmt = conn.prepareStatement(
                        "UPDATE user_session SET last_used = now() - make_interval(hours => ?) WHERE token = ?")) {
            stmt.setInt(1, hours);
            stmt.setString(2, token);
            stmt.executeUpdate();
        }
    }

    @Test
    void recentlyUsedSessionIsFound() {
        repository.createSession("recent", 1);
        Assertions.assertTrue(repository.byToken("recent", 720).isPresent());
    }

    @Test
    void unusedSessionExpires() throws SQLException {
        repository.createSession("stale", 2);
        lastUsedHoursAgo("stale", 721);
        Assertions.assertTrue(repository.byToken("stale", 720).isEmpty());
    }

    @Test
    void expiredSessionsAreDeleted() throws SQLException {
        repository.createSession("old", 3);
        repository.createSession("fresh", 3);
        lastUsedHoursAgo("old", 800);
        Assertions.assertEquals(1, repository.deleteExpired(720));
        Assertions.assertTrue(repository.byToken("fresh", 720).isPresent());
        Assertions.assertTrue(repository.byToken("old", 100000).isEmpty());
    }
}
