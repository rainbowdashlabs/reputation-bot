/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handler for the list type command.
 */
public class ListType implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a new ListType handler with the specified Guilds provider.
     *
     * @param guilds the Guilds provider
     */
    public ListType(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event to list the type of channels.
     *
     * @param event the SlashCommandInteractionEvent
     * @param context the EventContext
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var channels = guilds.guild(event.getGuild()).settings().thanking().channels();
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize(
                         channels.isWhitelist() ? "command.channel.listType.message.whitelist" : "command.channel.listType.message.blacklist"))
                 .queue();
            return;
        }
        var whitelist = "whitelist".equalsIgnoreCase(event.getOption("type").getAsString());

        event.reply(context.localize(
                     channels.listType(whitelist) ? "command.channel.listType.message.whitelist" : "command.channel.listType.message.blacklist"))
             .queue();
    }

    /**
     * Handles the auto-complete interaction event for the list type command.
     *
     * @param event the CommandAutoCompleteInteractionEvent
     * @param context the EventContext
     */
    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        if ("type".equals(event.getFocusedOption().getName())) {
            event.replyChoices(Completion.complete(event.getFocusedOption().getValue(), "whitelist", "blacklist"))
                 .queue();
        }
    }
}
