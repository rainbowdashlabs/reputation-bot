/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.actions.messages.log.handler;

import de.chojo.jdautil.interactions.message.MessageHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.log.handler.Analyzer;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

/**
 * Handler for analyzing messages in the context of a guild.
 */
public class MessageAnalyzer implements MessageHandler {
    private final Guilds guilds;

    /**
     * Constructs a new MessageAnalyzer handler.
     *
     * @param guilds the Guilds provider
     */
    public MessageAnalyzer(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the message context interaction event.
     *
     * @param event the MessageContextInteractionEvent
     * @param eventContext the EventContext
     */
    @Override
    public void onMessage(MessageContextInteractionEvent event, EventContext eventContext) {
        Analyzer.sendAnalyzerLog(event, guilds, event.getTarget().getIdLong(), eventContext);
    }
}
