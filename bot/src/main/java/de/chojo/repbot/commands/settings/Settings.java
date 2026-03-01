/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.settings;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.web.services.SessionService;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;

public class Settings extends SlashCommand {
    public Settings(SessionService sessionService) {
        super(Slash.of("settings", "command.settings.description")
                .guildOnly()
                .adminCommand()
                .command((event, ctx) -> {
                    event.reply(ctx.localize("command.settings.start"))
                            .addComponents(ActionRow.of(Button.link(
                                    sessionService.sessionUrl(event.getGuild().getIdLong()),
                                    ctx.localize("command.settings.button"))))
                            .setEphemeral(true)
                            .complete();
                }));
    }
}
