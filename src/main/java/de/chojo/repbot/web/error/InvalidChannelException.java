/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.error;

import io.javalin.http.HttpStatus;

public class InvalidChannelException extends ApiException{
    public InvalidChannelException(long id) {
        super(HttpStatus.BAD_REQUEST, "Invalid channel id: " + id);
    }
}
