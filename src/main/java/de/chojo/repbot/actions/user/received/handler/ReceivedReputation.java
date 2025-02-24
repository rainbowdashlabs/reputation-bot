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

/**
 * Handles the received reputation user context interaction.
 */
public class ReceivedReputation implements UserHandler {
    private final Guilds guilds;

    /**
     * Constructs a ReceivedReputation handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public ReceivedReputation(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the user context interaction event.
     *
     * @param event the user context interaction event
     * @param eventContext the event context
     */
    @Override
    public void onUser(UserContextInteractionEvent event, EventContext eventContext) {
        Received.send(event, event.getTargetMember(), guilds, eventContext);
    }
}
