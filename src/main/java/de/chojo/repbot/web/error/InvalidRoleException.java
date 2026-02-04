package de.chojo.repbot.web.error;

import io.javalin.http.HttpStatus;

public class InvalidRoleException extends ApiException{
    public InvalidRoleException(long id) {
        super(HttpStatus.BAD_REQUEST, "Invalid role id: " + id);
    }
}
