package de.chojo.repbot.web.error;

import io.javalin.http.HttpStatus;

public class InvalidChannelException extends ApiException{
    public InvalidChannelException(long id) {
        super(HttpStatus.BAD_REQUEST, "Invalid channel id: " + id);
    }
}
