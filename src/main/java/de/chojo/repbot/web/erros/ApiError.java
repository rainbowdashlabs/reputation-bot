package de.chojo.repbot.web.erros;

import io.javalin.http.HttpCode;

public class ApiError extends RuntimeException {
    private HttpCode status;

    public ApiError(HttpCode status, String message) {
        super(message);
        this.status = status;
    }

    public HttpCode status() {
        return status;
    }
}
