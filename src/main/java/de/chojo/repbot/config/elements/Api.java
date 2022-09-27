package de.chojo.repbot.config.elements;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "MismatchedReadAndWriteOfArray"})
public class Api {
    private String host = "0.0.0.0";
    private int port = 8888;
    private String url = "https://repbot.chojo.de";

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String url() {
        return url;
    }
}
