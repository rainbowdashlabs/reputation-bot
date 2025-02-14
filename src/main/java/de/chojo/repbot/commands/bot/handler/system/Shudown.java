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
 * Handler for the "shutdown" slash command.
 * This command shuts down the bot if the user is the owner.
 */
public class Shudown implements SlashHandler {
    private static final Logger log = getLogger(Shudown.class);
    private final Configuration configuration;

    /**
     * Constructs a new Shudown handler.
     *
     * @param configuration the bot configuration
     */
    public Shudown(Configuration configuration) {
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
        log.info(LogNotify.STATUS, "Shutdown command received from {}. Shutting down.", event.getUser().getAsTag());
        event.reply("Initializing shutdown. Good bye :c").complete();
        System.exit(0);
    }
}
