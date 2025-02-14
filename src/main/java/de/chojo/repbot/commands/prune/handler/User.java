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

/**
 * Handler for the prune user slash command.
 */
public class User implements SlashHandler {
    /**
     * Service for handling GDPR-related operations.
     */
    private final GdprService service;

    /**
     * Constructs a new User handler.
     *
     * @param service the GDPR service used for cleanup operations
     */
    public User(GdprService service) {
        this.service = service;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
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
