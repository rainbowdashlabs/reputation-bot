/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Channels;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handler for the "set channel" slash command.
 * This command sets the text channel or category for the guild's settings.
 */
public class Set extends BaseChannelModifier {
    /**
     * Constructs a new Set handler.
     *
     * @param guilds the guilds provider
     */
    public Set(Guilds guilds) {
        super(guilds);
    }

    /**
     * Sets the text channel for the guild's settings.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param channels the channels settings
     * @param channel the text channel to set
     */
    @Override
    public void textChannel(SlashCommandInteractionEvent event, EventContext context, Channels channels, StandardGuildChannel channel) {
        channels.clearChannel();
        channels.add(channel);
        event.getHook().editOriginal(
                context.localize("command.channel.set.message.set",
                        Replacement.create("CHANNEL", channel.getAsMention()))).queue();
    }

    /**
     * Sets the category for the guild's settings.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param channels the channels settings
     * @param category the category to set
     */
    @Override
    public void category(SlashCommandInteractionEvent event, EventContext context, Channels channels, Category category) {
        channels.clearCategories();
        channels.add(category);
        event.getHook().editOriginal(
                context.localize("command.channel.set.message.set",
                        Replacement.create("CHANNEL", category.getAsMention()))).queue();
    }
}
