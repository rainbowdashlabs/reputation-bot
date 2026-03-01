/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.actions.messages.log.handler;

import de.chojo.jdautil.interactions.message.MessageHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.log.handler.Analyzer;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public class MessageAnalyzer implements MessageHandler {
    private final GuildRepository guildRepository;

    public MessageAnalyzer(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onMessage(MessageContextInteractionEvent event, EventContext eventContext) {
        Analyzer.sendAnalyzerLog(event, guildRepository, event.getTarget().getIdLong(), eventContext);
    }
}
