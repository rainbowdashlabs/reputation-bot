/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler.system;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.util.LogNotify;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Handler for the upgrade slash command.
 */
public class Upgrade implements SlashHandler {
    /**
     * Logger instance for logging events.
     */
    private static final Logger log = getLogger(Upgrade.class);

    /**
     * Configuration object for accessing base settings.
     */
    private final Configuration configuration;

    /**
     * Constructs a new Upgrade handler.
     *
     * @param configuration the configuration object used to access base settings
     */
    public Upgrade(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (!configuration.baseSettings().isOwner(event.getUser().getIdLong())) {
            event.reply("No.").setEphemeral(true).queue();
            return;
        }
        log.info(LogNotify.STATUS, "Upgrade command received from {}. Attempting upgrade.", event.getUser().getAsTag());
        event.reply("Starting upgrade. Will be back soon!").complete();
        System.exit(20);
    }
}
