/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for the base settings.
 */
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal", "MismatchedQueryAndUpdateOfCollection"})
public class BaseSettings {
    private String token = "";
    private List<Long> botOwner = new ArrayList<>();
    private long botGuild = 0L;

    /**
     * Creates a new base configuration with default values.
     */
    public BaseSettings(){
    }

    /**
     * Gets the bot token.
     *
     * @return the bot token
     */
    public String token() {
        return token;
    }

    /**
     * Checks if the given ID is a bot owner.
     *
     * @param id the ID to check
     * @return true if the ID is a bot owner, false otherwise
     */
    public boolean isOwner(long id) {
        return botOwner.contains(id);
    }

    /**
     * Gets the bot guild ID.
     *
     * @return the bot guild ID
     */
    public long botGuild() {
        return botGuild;
    }
}
