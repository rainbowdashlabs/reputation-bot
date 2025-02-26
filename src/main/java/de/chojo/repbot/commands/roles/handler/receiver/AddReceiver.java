/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.roles.handler.receiver;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;

/**
 * Handler for the "add receiver" slash command, which adds a role as a receiver for thankwords.
 */
public class AddReceiver implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs an AddReceiver handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public AddReceiver(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var role = event.getOption("role").getAsRole();
        guilds.guild(event.getGuild()).settings().thanking().receiverRoles().add(role);
        event.reply(context.localize("command.roles.receiver.add.message.add",
                Replacement.createMention(role))).setAllowedMentions(Collections.emptyList()).queue();
    }
}
