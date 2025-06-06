/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.actions.user.donated.received.handler;

import de.chojo.jdautil.interactions.user.UserHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.log.handler.Donated;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

public class DonatedReputation implements UserHandler {
    private final GuildRepository guildRepository;
    private final Configuration configuration;

    public DonatedReputation(GuildRepository guildRepository, Configuration configuration) {
        this.guildRepository = guildRepository;
        this.configuration = configuration;
    }


    @Override
    public void onUser(UserContextInteractionEvent event, EventContext eventContext) {
        Donated.send(event, event.getTargetMember(), guildRepository, eventContext, configuration);
    }
}
