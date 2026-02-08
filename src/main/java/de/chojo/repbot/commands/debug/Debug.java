/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.debug;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.web.sessions.SessionService;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;

public class Debug extends SlashCommand {

    public Debug(SessionService sessionService) {
        super(Slash.of("debug", "command.debug.description")
                .guildOnly()
                .adminCommand()
                .command((event, ctx) ->{
                    GuildSession guildSession = sessionService.getGuildSession(event.getGuild(), event.getMember());
                    event.reply(ctx.localize("command.debug.start"))
                         .addComponents(ActionRow.of(
                                 Button.link(guildSession.debugUrl(), ctx.localize("words.dashboard"))))
                         .setEphemeral(true)
                         .complete();
                }));
    }
}
