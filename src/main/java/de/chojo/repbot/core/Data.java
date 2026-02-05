/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.core;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.Analyzer;
import de.chojo.repbot.dao.access.Cleanup;
import de.chojo.repbot.dao.access.Gdpr;
import de.chojo.repbot.dao.access.gdpr.RemovalTask;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.provider.Voice;
import de.chojo.repbot.util.LogNotify;
import de.chojo.repbot.web.sessions.SessionService;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.mapper.rowmapper.RowMapper;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.postgresql.mapper.PostgresqlMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.updater.QueryReplacement;
import de.chojo.sadu.updater.SqlUpdater;
import org.slf4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

import static org.slf4j.LoggerFactory.getLogger;

public class Data {
    private static final Logger log = getLogger(Data.class);
    private final Threading threading;
    private final Configuration configuration;
    private HikariDataSource dataSource;
    private GuildRepository guildRepository;
    private Gdpr gdpr;
    private Cleanup cleanup;
    private Metrics metrics;
    private Voice voice;
    private Analyzer analyzer;

    private Data(Threading threading, Configuration configuration) {
        this.threading = threading;
        this.configuration = configuration;
    }

    public static Data create(Threading threading, Configuration configuration) throws SQLException, IOException {
        var data = new Data(threading, configuration);
        data.init();
        return data;
    }

    public void init() throws SQLException, IOException {
        initConnection();
        configure();
        updateDatabase();
        initDao();
    }

    public void initConnection() {
        try {
            dataSource = getConnectionPool();
        } catch (Exception e) {
            log.error("Could not connect to database. Retrying in 10.");
            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException ignore) {
            }
            initConnection();
        }
    }

    public GuildRepository guildRepository() {
        return guildRepository;
    }

    public Gdpr gdpr() {
        return gdpr;
    }

    public Cleanup cleanup() {
        return cleanup;
    }

    public Metrics metrics() {
        return metrics;
    }

    public Voice voice() {
        return voice;
    }

    public void shutDown() {
        dataSource.close();
    }

    public Analyzer analyzer() {
        return analyzer;
    }

    private void updateDatabase() throws IOException, SQLException {
        var schema = configuration.database().schema();
        SqlUpdater.builder(dataSource, PostgreSql.get())
                .setReplacements(new QueryReplacement("repbot_schema", schema))
                .setVersionTable(schema + ".repbot_version")
                .setSchemas(schema)
                .execute();
    }

    private void configure() {
        log.info("Configuring Query Configuration");
        var logger = getLogger("DbLogger");
        var registry = new RowMapperRegistry();
        registry.register(
                RowMapper.forClass(RemovalTask.class).mapper(RemovalTask::build).build());
        registry.register(PostgresqlMapper.getDefaultMapper());
        QueryConfiguration.setDefault(QueryConfiguration.builder(dataSource)
                .setExceptionHandler(
                        err -> logger.error(LogNotify.NOTIFY_ADMIN, "An error occurred during a database request", err))
                .setRowMapperRegistry(registry)
                .build());
    }

    private void initDao() {
        log.info("Creating DAOs");
        guildRepository = new GuildRepository(configuration);
        gdpr = new Gdpr(configuration);
        cleanup = new Cleanup();
        metrics = new Metrics(dataSource);
        analyzer = new Analyzer(configuration);
        voice = new Voice(configuration);
    }

    private HikariDataSource getConnectionPool() {
        log.info("Creating connection pool.");
        var data = configuration.database();
        return DataSourceCreator.create(PostgreSql.get())
                .configure(config -> config.host(data.host())
                        .port(data.port())
                        .user(data.user())
                        .password(data.password())
                        .database(data.database()))
                .create()
                .withMaximumPoolSize(data.poolSize())
                .withThreadFactory(Threading.createThreadFactory(threading.hikariGroup()))
                .forSchema(data.schema())
                .build();
    }
}
