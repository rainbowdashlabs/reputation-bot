package de.chojo.repbot.config.elements;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "MismatchedReadAndWriteOfArray"})
public class Api {
    private String host = "0.0.0.0";
    private int port = 8888;

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }
}
