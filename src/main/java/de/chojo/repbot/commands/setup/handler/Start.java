/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.setup.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.web.sessions.SessionService;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Start implements SlashHandler {
    private final SessionService sessionService;

    public Start(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        GuildSession guildSession = sessionService.getGuildSession(event.getGuild(), event.getMember());
        event.reply(context.localize("command.setup.start"))
                .addComponents(
                        ActionRow.of(Button.link(guildSession.setupUrl(), context.localize("command.setup.button"))))
                .setEphemeral(true)
                .complete();
    }
}
