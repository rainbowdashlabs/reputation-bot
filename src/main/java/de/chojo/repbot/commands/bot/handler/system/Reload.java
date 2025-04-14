/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler.system;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.exception.ConfigurationException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handles the reload command for reloading the bot's configuration.
 */
public class Reload implements SlashHandler {

    private final Configuration configuration;

    /**
     * Constructs a Reload handler with the specified configuration.
     *
     * @param configuration the configuration to be reloaded
     */
    public Reload(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Handles the slash command interaction event for reloading the configuration.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        try {
            configuration.reload();
        } catch (ConfigurationException e) {
            event.reply("Config reload failed").setEphemeral(true).queue();
            return;
        }
        event.reply("Config file reloaded.").setEphemeral(true).queue();
    }
}
