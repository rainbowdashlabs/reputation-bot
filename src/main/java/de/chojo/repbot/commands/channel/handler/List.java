/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Channels;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.stream.Collectors;

/**
 * Handler for the list channels command.
 */
public class List implements SlashHandler {
    private static final String MORE = String.format("$%s$", "command.channel.list.message.more");
    private final Guilds guilds;

    /**
     * Constructs a new List handler.
     *
     * @param guilds the Guilds provider
     */
    public List(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event to list channels.
     *
     * @param event the SlashCommandInteractionEvent
     * @param context the EventContext
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var channels = guilds.guild(event.getGuild()).settings().thanking().channels();
        event.replyEmbeds(getChannelList(channels, context)).queue();
    }

    /**
     * Generates a MessageEmbed containing the list of channels and categories.
     *
     * @param channels the Channels instance containing the channel and category data
     * @param context the EventContext
     * @return a MessageEmbed with the list of channels and categories
     */
    private MessageEmbed getChannelList(Channels channels, EventContext context) {

        var channelNames = channels.channels().stream()
                .map(IMentionable::getAsMention)
                .limit(40)
                .collect(Collectors.joining(", "));
        if (channels.channels().size() > 40) {
            channelNames += MORE;
        }

        var categoryNames = channels.categories().stream()
                .map(IMentionable::getAsMention)
                .limit(40)
                .collect(Collectors.joining(", "));
        if (channels.categories().size() > 40) {
            categoryNames += MORE;
        }

        return new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle(channels.isWhitelist() ? "command.channel.list.message.whitelist" : "command.channel.list.message.blacklist")
                .addField("words.channels", channelNames, false,
                        Replacement.create("MORE", channels.channels().size() - 40))
                .addField("words.categories", categoryNames, false,
                        Replacement.create("MORE", channels.channels().size() - 40))
                .build();
    }

}
