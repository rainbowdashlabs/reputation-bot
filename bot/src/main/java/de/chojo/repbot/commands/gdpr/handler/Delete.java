/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.gdpr.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.Gdpr;
import de.chojo.repbot.dao.access.user.RepUser;
import de.chojo.repbot.dao.provider.UserRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Delete implements SlashHandler {
    private static final Logger log = getLogger(Delete.class);
    private final Gdpr gdpr;
    private final UserRepository repository;

    public Delete(Gdpr gdpr, UserRepository repository) {
        this.gdpr = gdpr;
        this.repository = repository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        RepUser repUser = repository.byUser(event.getUser());
        boolean anyMatch = repUser.purchases().all().stream().anyMatch(p -> p.guildId() != 0);
        if (anyMatch) {
            event.reply(context.localize("command.gdpr.delete.message.purchased"))
                    .setEphemeral(true)
                    .complete();
            return;
        }

        var success = gdpr.request(event.getUser()).queueDeletion();
        if (success) {
            event.reply(context.localize("command.gdpr.delete.message.received"))
                    .setEphemeral(true)
                    .complete();
        } else {
            event.reply(context.localize("command.gdpr.delete.message.scheduled"))
                    .setEphemeral(true)
                    .complete();
        }
    }
}
