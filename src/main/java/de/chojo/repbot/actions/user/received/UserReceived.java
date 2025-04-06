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
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.util.Set;

public class UserReceived implements UserProvider<User> {
    private final Guilds guilds;

    public UserReceived(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public User user() {
        return User.of("Received Reputation")
                   .handler(new ReceivedReputation(guilds))
                   .setContext(Set.of(InteractionContextType.GUILD))
                   .withPermission(Permission.MESSAGE_MANAGE)
                   .build();
    }
}
