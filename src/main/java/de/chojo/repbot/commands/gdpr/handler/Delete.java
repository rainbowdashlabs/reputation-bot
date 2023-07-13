/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.gdpr.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.Gdpr;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Delete implements SlashHandler {
    private final Gdpr gdpr;
    private static final Logger log = getLogger(Delete.class);

    public Delete(Gdpr gdpr) {
        this.gdpr = gdpr;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var success = gdpr.request(event.getUser()).queueDeletion();
        if (success) {
            event.reply(context.localize("command.gdpr.delete.message.received")).setEphemeral(true).queue();
        } else {
            event.reply(context.localize("command.gdpr.delete.message.scheduled")).setEphemeral(true).queue();
        }
    }
}
