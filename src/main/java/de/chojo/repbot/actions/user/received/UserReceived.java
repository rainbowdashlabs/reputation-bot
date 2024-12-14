/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.actions.user.received;

import de.chojo.jdautil.interactions.user.User;
import de.chojo.jdautil.interactions.user.provider.UserProvider;
import de.chojo.repbot.actions.user.received.handler.ReceivedReputation;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.Permission;

/**
 * Class for handling user received actions.
 */
public class UserReceived implements UserProvider<User> {
    /**
     * Provider for accessing guild data.
     */
    private final Guilds guilds;

    /**
     * Constructs a new UserReceived instance.
     *
     * @param guilds the guilds provider
     */
    public UserReceived(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Creates and returns a User object configured for received reputation.
     *
     * @return the configured User object
     */
    @Override
    public User user() {
        return User.of("Received Reputation").handler(new ReceivedReputation(guilds))
                   .setGuildOnly(true)
                   .withPermission(Permission.MESSAGE_MANAGE)
                   .build();
    }
}
