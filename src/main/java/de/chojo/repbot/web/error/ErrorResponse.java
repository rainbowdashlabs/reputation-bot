/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.error;

/**
 * Generic error response structure for API errors.
 */
public class ErrorResponse {
    private final String error;
    private final String message;
    private final Object details;

    public ErrorResponse(String error, String message, Object details) {
        this.error = error;
        this.message = message;
        this.details = details;
    }

    public ErrorResponse(String error, String message) {
        this(error, message, null);
    }

    public String error() {
        return error;
    }

    public String message() {
        return message;
    }

    public Object details() {
        return details;
    }
}
