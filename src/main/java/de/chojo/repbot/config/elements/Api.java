/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

/**
 * Configuration class for the API settings.
 */
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Api {
    private String host = "0.0.0.0";
    private int port = 8888;
    private String url = "https://repbot.chojo.de";

    /**
     * Creates a new API configuration with default values.
     */
    public Api(){
    }

    /**
     * Gets the host address for the API.
     *
     * @return the host address
     */
    public String host() {
        return host;
    }

    /**
     * Gets the port number for the API.
     *
     * @return the port number
     */
    public int port() {
        return port;
    }

    /**
     * Gets the URL for the API.
     *
     * @return the URL
     */
    public String url() {
        return url;
    }
}
