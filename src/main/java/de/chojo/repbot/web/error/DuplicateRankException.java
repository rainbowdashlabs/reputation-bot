/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.error;

import io.javalin.http.HttpStatus;

public class DuplicateRankException extends ApiException {
    public DuplicateRankException(long roleId) {
        super(HttpStatus.BAD_REQUEST, "Duplicate rank for role id: " + roleId);
    }
}
