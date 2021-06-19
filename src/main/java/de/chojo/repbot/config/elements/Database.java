package de.chojo.repbot.config.elements;


@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Database {
    private String host = "localhost";
    private String port = "5432";
    private String database = "db";
    private String schema = "repbot";
    private String user = "user";
    private String password = "pw";
    private int poolSize = 5;

    public String host() {
        return host;
    }

    public String port() {
        return port;
    }

    public String database() {
        return database;
    }

    public String schema() {
        return schema;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    public int poolSize() {
        return poolSize;
    }
}
