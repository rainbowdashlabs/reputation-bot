/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dataconsistency;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.postgresql.mapper.PostgresqlMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.updater.SqlUpdater;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A PostgreSQL container with the bot schema deployed and set as the default query configuration.
 *
 * @param container  the running container
 * @param dataSource a data source on the bot schema
 */
public record TestDatabase(GenericContainer<?> container, HikariDataSource dataSource) implements AutoCloseable {

    /**
     * Starts a container, deploys the schema and points the default query configuration at it.
     *
     * @return the started database
     */
    public static TestDatabase start() throws IOException, SQLException {
        var container = TestSQL.createContainer("postgres", "postgres");
        var dataSource = DataSourceCreator.create(PostgreSql.get())
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
        return new TestDatabase(container, dataSource);
    }

    /**
     * Runs plain SQL statements, for example to prepare test data.
     *
     * @param statements the statements to run
     */
    public void execute(String... statements) throws SQLException {
        try (var conn = dataSource.getConnection();
                var stmt = conn.createStatement()) {
            for (var statement : statements) {
                stmt.execute(statement);
            }
        }
    }

    @Override
    public void close() {
        dataSource.close();
        container.stop();
    }
}
