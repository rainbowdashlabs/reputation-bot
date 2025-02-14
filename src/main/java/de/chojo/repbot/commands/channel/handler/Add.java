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
 * Handler for the "add channel" slash command, which adds a channel to the thanking channels.
 */
public class Add extends BaseChannelModifier {

    /**
     * Constructs an Add handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public Add(Guilds guilds) {
        super(guilds);
    }

    /**
     * Adds a text channel to the thanking channels.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param channels the channels settings
     * @param channel the text channel to add
     */
    @Override
    public void textChannel(SlashCommandInteractionEvent event, EventContext context, Channels channels, StandardGuildChannel channel) {
        channels.add(channel);
        event.getHook().editOriginal(
                context.localize("command.channel.add.message.added",
                        Replacement.create("CHANNEL", channel.getAsMention()))).queue();
    }

    /**
     * Adds a category to the thanking channels.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param channels the channels settings
     * @param category the category to add
     */
    @Override
    public void category(SlashCommandInteractionEvent event, EventContext context, Channels channels, Category category) {
        channels.add(category);
        event.getHook().editOriginal(
                context.localize("command.channel.add.message.added",
                        Replacement.create("CHANNEL", category.getAsMention()))).queue();
    }
}
