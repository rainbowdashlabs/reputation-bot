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
 * Handles the removal of channels from a guild's settings.
 */
public class Remove extends BaseChannelModifier {

    /**
     * Constructs a Remove handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public Remove(Guilds guilds) {
        super(guilds);
    }

    /**
     * Handles the removal of a text channel from the guild's settings.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param channels the channels settings
     * @param channel the text channel to remove
     */
    @Override
    public void textChannel(SlashCommandInteractionEvent event, EventContext context, Channels channels, StandardGuildChannel channel) {
        channels.remove(channel);
        event.getHook().editOriginal(
                context.localize("command.channel.remove.message.removed",
                        Replacement.create("CHANNEL", channel.getAsMention()))).queue();
    }

    /**
     * Handles the removal of a category from the guild's settings.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param channels the channels settings
     * @param category the category to remove
     */
    @Override
    public void category(SlashCommandInteractionEvent event, EventContext context, Channels channels, Category category) {
        channels.remove(category);
        event.getHook().editOriginal(
                context.localize("command.channel.remove.message.removed",
                        Replacement.create("CHANNEL", category.getAsMention()))).queue();
    }
}
