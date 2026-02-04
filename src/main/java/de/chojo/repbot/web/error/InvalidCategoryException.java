package de.chojo.repbot.web.error;

import io.javalin.http.HttpStatus;

public class InvalidCategoryException extends ApiException{
    public InvalidCategoryException(long id) {
        super(HttpStatus.BAD_REQUEST, "Invalid category id: " + id);
    }
}
