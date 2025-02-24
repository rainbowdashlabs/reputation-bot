/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler.log;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.log.handler.BaseAnalyzer;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handler for the "analyzer" slash command.
 */
public class Analyzer extends BaseAnalyzer implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs an Analyzer handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public Analyzer(Guilds guilds) {
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
        var guild_id = ValueParser.parseLong(event.getOption("guild_id").getAsString());
        onSlashCommand(event, context, guilds.byId(guild_id.get()).reputation());
    }
}
