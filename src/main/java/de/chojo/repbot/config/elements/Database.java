/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

/**
 * Configuration class for the database settings.
 */
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class Database {
    /**
     * The host of the database.
     */
    private String host = "localhost";

    /**
     * The port of the database.
     */
    private String port = "5432";

    /**
     * The name of the database.
     */
    private String database = "db";

    /**
     * The schema of the database.
     */
    private String schema = "repbot";

    /**
     * The user for the database connection.
     */
    private String user = "user";

    /**
     * The password for the database connection.
     */
    private String password = "pw";

    /**
     * The size of the connection pool.
     */
    private int poolSize = 5;

    /**
     * Creates a new database configuration with default values.
     */
    public Database(){
    }

    /**
     * Retrieves the database host.
     *
     * @return the database host
     */
    public String host() {
        return host;
    }

    /**
     * Retrieves the database port.
     *
     * @return the database port
     */
    public String port() {
        return port;
    }

    /**
     * Retrieves the database name.
     *
     * @return the database name
     */
    public String database() {
        return database;
    }

    /**
     * Retrieves the database schema.
     *
     * @return the database schema
     */
    public String schema() {
        return schema;
    }

    /**
     * Retrieves the database user.
     *
     * @return the database user
     */
    public String user() {
        return user;
    }

    /**
     * Retrieves the database password.
     *
     * @return the database password
     */
    public String password() {
        return password;
    }

    /**
     * Retrieves the size of the connection pool.
     *
     * @return the connection pool size
     */
    public int poolSize() {
        return poolSize;
    }
}
