/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.web.sessions.GuildSession;
import de.chojo.repbot.web.sessions.SessionService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Session implements SlashHandler {
    private final SessionService sessionService;

    public Session(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Guild guildId = event.getJDA().getShardManager().getGuildById(event.getOption("guild_id").getAsString());
        GuildSession guildSession = sessionService.getGuildSession(guildId, event.getMember());
        event.reply(guildSession.sessionUrl()).setEphemeral(true).complete();
    }
}
