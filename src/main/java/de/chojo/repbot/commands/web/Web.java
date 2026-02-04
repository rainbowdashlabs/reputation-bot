/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.web;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.web.sessions.GuildSession;
import de.chojo.repbot.web.sessions.SessionService;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;

public class Web extends SlashCommand {
    public Web(SessionService sessionService) {
        super(Slash.of("web", "command.web.description")
                   .command((event, ctx) -> {
                       GuildSession guildSession = sessionService.getGuildSession(event.getGuild(), event.getMember());
                       event.reply("Session created")
                            .addComponents(ActionRow.of(Button.link(guildSession.sessionUrl(), "Open Session")))
                            .setEphemeral(true)
                            .complete();
                   }));
    }
}
