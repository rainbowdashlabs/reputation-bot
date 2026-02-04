/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import de.chojo.jdautil.util.SysVar;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class Database {
    private String host = "localhost";
    private String port = "5432";
    private String database = "postgres";
    private String schema = "repbot_schema";
    private String user = "postgres";
    private String password = "postgres";
    private int poolSize = 5;

    public String host() {
        return SysVar.envOrProp("BOT_DB_HOST", "bot.db.host", host);
    }

    public String port() {
        return SysVar.envOrProp("BOT_DB_PORT", "bot.db.port", port);
    }

    public String database() {
        return SysVar.envOrProp("BOT_DB_DATABASE", "bot.db.database", database);
    }

    public String schema() {
        return SysVar.envOrProp("BOT_DB_SCHEMA", "bot.db.schema", schema);
    }

    public String user() {
        return SysVar.envOrProp("BOT_DB_USER", "bot.db.user", user);
    }

    public String password() {
        return SysVar.envOrProp("BOT_DB_PASSWORD", "bot.db.password", password);
    }

    public int poolSize() {
        return Integer.parseInt(SysVar.envOrProp("BOT_DB_POOLSIZE", "bot.db.poolsize", String.valueOf(poolSize)));
    }
}
