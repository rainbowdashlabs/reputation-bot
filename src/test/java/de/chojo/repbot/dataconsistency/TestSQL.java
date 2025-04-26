/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dataconsistency;

<<<<<<< HEAD
import com.zaxxer.hikari.HikariDataSource;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.testing.SaduTests;
import de.chojo.sadu.updater.SqlUpdater;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.sql.SQLException;

public class TestSQL {
    @Test
    void verifyStructure() throws IOException {
        SaduTests.execute(1, PostgreSql.get());
    }

    @Test
    void verifyDeployment() throws IOException, SQLException {
        try (var container = createContainer("postgres", "postgres")) {
            HikariDataSource dataSource = DataSourceCreator.create(PostgreSql.get())
                                                           .configure(conf -> {
                                                               conf.port(container.getFirstMappedPort());
                                                           }).create()
                                                           .usingUsername("postgres")
                                                           .usingPassword("postgres")
                                                           .build();
            try (dataSource) {
                SqlUpdater.builder(dataSource, PostgreSql.get())
                        .setSchemas("repbot_schema")
                        .execute();
            }
        }
    }

    public static GenericContainer<?> createContainer(String user, String pw) {
        GenericContainer<?> self = new GenericContainer<>(DockerImageName.parse("postgres:latest"))
                .withExposedPorts(5432)
                .withEnv("POSTGRES_USER", user)
                .withEnv("POSTGRES_PASSWORD", pw)
                .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2));
        self.start();
        return self;
    }
=======
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.testing.SaduTests;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestSQL {
    @Test
    void test() throws IOException {
        SaduTests.execute(1, PostgreSql.get());
    }
>>>>>>> 75aef07 (Implement features #668, #669 #675)
}
