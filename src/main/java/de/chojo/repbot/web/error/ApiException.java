/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.error;

import io.javalin.http.HttpCode;

/**
 * Custom exception class for API errors.
 */
public class ApiException extends RuntimeException {
    /**
     * The HTTP status code associated with this exception.
     */
    private final HttpCode status;

    /**
     * Constructs a new ApiException with the specified HTTP status code and message.
     *
     * @param status  the HTTP status code
     * @param message the detail message
     */
    public ApiException(HttpCode status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Returns the HTTP status code associated with this exception.
     *
     * @return the HTTP status code
     */
    public HttpCode status() {
        return status;
    }
}
