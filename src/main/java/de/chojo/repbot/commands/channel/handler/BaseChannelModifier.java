/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Channels;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Set;

/**
 * Abstract base class for handling slash commands that modify channels.
 */
public abstract class BaseChannelModifier implements SlashHandler {
    private static final java.util.Set<ChannelType> ALLOWED_CHANNEL =
            Set.of(ChannelType.TEXT, ChannelType.FORUM, ChannelType.CATEGORY, ChannelType.VOICE);
    private static final java.util.Set<ChannelType> TEXT_CHANNEL =
            Set.of(ChannelType.TEXT, ChannelType.FORUM, ChannelType.VOICE);
    private final Guilds guilds;

    /**
     * Constructs a BaseChannelModifier with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public BaseChannelModifier(Guilds guilds) {
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
        var channels = guilds.guild(event.getGuild()).settings().thanking().channels();
        var channelType = event.getOption("channel").getChannelType();
        if (!ALLOWED_CHANNEL.contains(channelType)) {
            event.reply(context.localize("error.onlyTextOrCategory")).setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        var channel = event.getOption("channel").getAsChannel();
        if (TEXT_CHANNEL.contains(channelType)) {
            textChannel(event, context, channels, channel.asStandardGuildChannel());
        } else {
            category(event, context, channels, channel.asCategory());
        }
    }

    /**
     * Abstract method to handle text channel modification.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param channels the channels settings
     * @param channel the text channel to modify
     */
    public abstract void textChannel(SlashCommandInteractionEvent event, EventContext context, Channels channels, StandardGuildChannel channel);

    /**
     * Abstract method to handle category modification.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param channels the channels settings
     * @param category the category to modify
     */
    public abstract void category(SlashCommandInteractionEvent event, EventContext context, Channels channels, Category category);
}
