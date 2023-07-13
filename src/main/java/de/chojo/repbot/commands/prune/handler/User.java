/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.prune.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.service.GdprService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class User implements SlashHandler {
    private final GdprService service;

    public User(GdprService service) {
        this.service = service;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var user = event.getOption("user");
        if (user != null) {
            service.cleanupGuildUser(event.getGuild(), user.getAsUser().getIdLong());
            event.reply(context.localize("command.prune.user.message.removed")).queue();
            return;
        }

        user = event.getOption("userid");
        if (user != null) {
            var idRaw = Verifier.getIdRaw(user.getAsString());
            if (idRaw.isPresent()) {
                service.cleanupGuildUser(event.getGuild(), Long.valueOf(idRaw.get()));
                event.reply(context.localize("command.prune.user.message.removed")).queue();
                return;
            }
            event.reply(context.localize("error.userNotFound")).setEphemeral(true).queue();
        }
    }
}
