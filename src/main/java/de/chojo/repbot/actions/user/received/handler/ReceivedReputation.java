/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.actions.user.received.handler;

import de.chojo.jdautil.interactions.user.UserHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.log.handler.Received;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

public class ReceivedReputation implements UserHandler {
    private final Guilds guilds;

    public ReceivedReputation(Guilds guilds) {
        this.guilds = guilds;
    }


    @Override
    public void onUser(UserContextInteractionEvent event, EventContext eventContext) {
        Received.send(event, event.getTargetMember(), guilds, eventContext);
    }
}
