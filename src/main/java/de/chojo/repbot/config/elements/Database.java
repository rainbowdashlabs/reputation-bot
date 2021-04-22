package de.chojo.repbot.config.elements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Database {
    private String host = "localhost";
    private String port = "5432";
    private String database = "db";
    private String schema = "repbot";
    private String user = "user";
    private String password = "pw";
    private int poolSize = 5;
}
