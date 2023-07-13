/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.erros;

import io.javalin.http.HttpCode;

public class ApiException extends RuntimeException {
    private final HttpCode status;

    public ApiException(HttpCode status, String message) {
        super(message);
        this.status = status;
    }

    public HttpCode status() {
        return status;
    }
}
