/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.actions.user.donated.received;

import de.chojo.jdautil.interactions.user.User;
import de.chojo.jdautil.interactions.user.provider.UserProvider;
import de.chojo.repbot.actions.user.donated.received.handler.DonatedReputation;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.util.Set;

public class UserDonated implements UserProvider<User> {
    private final GuildRepository guildRepository;

    public UserDonated(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public User user() {
        return User.of("Given Reputation").handler(new DonatedReputation(guildRepository))
                   .setContext(Set.of(InteractionContextType.GUILD))
                   .withPermission(Permission.MESSAGE_MANAGE)
                   .build();
    }
}
