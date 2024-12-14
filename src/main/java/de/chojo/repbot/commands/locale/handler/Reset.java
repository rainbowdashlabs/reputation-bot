/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.locale.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handler for the reset locale command.
 */
public class Reset implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a new Reset handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public Reset(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event to reset the locale.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        guilds.guild(event.getGuild()).settings().general().language(null);
        event.reply("command.locale.reset.message.changed").queue();
    }
}
