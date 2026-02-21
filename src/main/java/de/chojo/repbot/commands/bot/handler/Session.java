/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.web.services.SessionService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Session implements SlashHandler {
    private final SessionService sessionService;

    public Session(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        long guildId = event.getOption("guild_id").getAsLong();
        event.reply(sessionService.sessionUrl(guildId)).setEphemeral(true).complete();
    }
}
