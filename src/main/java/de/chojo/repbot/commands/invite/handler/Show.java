/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.invite.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handler for the "show invite" slash command.
 * This command displays an invite link for the bot.
 */
public class Show implements SlashHandler {
    private final Configuration configuration;

    /**
     * Constructs a new Show handler.
     *
     * @param configuration the bot configuration
     */
    public Show(Configuration configuration) {
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
        event.replyEmbeds(getResponse(context)).queue();
    }

    /**
     * Generates the response embed message for the slash command.
     *
     * @param context the event context
     * @return the response embed message
     */
    @NotNull
    private MessageEmbed getResponse(EventContext context) {
        return new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.invite.message.title")
                .setDescription("command.invite.message.click", Replacement.create("URL", configuration.links()
                                                                                                       .invite()))
                .build();
    }
}
