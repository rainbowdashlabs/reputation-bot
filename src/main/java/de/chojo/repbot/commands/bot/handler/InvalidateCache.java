/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handler for the invalidate cache command.
 */
public class InvalidateCache implements SlashHandler {

    private final Guilds guilds;

    /**
     * Constructs a new InvalidateCache handler.
     *
     * @param guilds the Guilds provider
     */
    public InvalidateCache(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event to invalidate the guild cache.
     *
     * @param event the SlashCommandInteractionEvent
     * @param context the EventContext
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        guilds.invalidate(event.getOption("guild").getAsLong());
        event.reply("Invalidated guild cache").queue();
    }
}
