/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.error;

import io.javalin.http.HttpStatus;

public class InvalidCategoryException extends ApiException{
    public InvalidCategoryException(long id) {
        super(HttpStatus.BAD_REQUEST, "Invalid category id: " + id);
    }
}
