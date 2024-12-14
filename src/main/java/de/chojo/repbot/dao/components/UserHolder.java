/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.components;

import net.dv8tion.jda.api.entities.User;

/**
 * Interface representing a holder for a user, providing access to the user information.
 */
public interface UserHolder {
    /**
     * Retrieves the user associated with this holder.
     *
     * @return the user
     */
    User user();

    /**
     * Retrieves the ID of the user associated with this holder.
     *
     * @return the user ID
     */
    default long userId() {
        return user().getIdLong();
    }
}
