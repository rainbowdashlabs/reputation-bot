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
