/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.error;

import io.javalin.http.HttpStatus;

/**
 * Thrown when the guild of a session can no longer be reached, because the bot left it or it is unavailable.
 */
public class GuildUnavailableException extends ApiException {
    public GuildUnavailableException(long guildId) {
        super(HttpStatus.NOT_FOUND, "Guild not available: " + guildId);
    }
}
