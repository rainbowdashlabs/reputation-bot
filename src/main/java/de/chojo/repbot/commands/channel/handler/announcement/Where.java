/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler.announcement;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/**
 * Handler for the "Where" slash command.
 */
public class Where implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a new Where handler.
     *
     * @param guilds the guilds provider
     */
    public Where(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event   the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var announcements = guilds.guild(event.getGuild()).settings().announcements();
        if (announcements.sameChannel("same channel".equalsIgnoreCase(event.getOption("where", OptionMapping::getAsString)))) {
            event.reply(context.localize("command.channel.announcement.location.message.samechannel")).queue();
        } else {
            event.reply(context.localize("command.channel.announcement.location.message.otherchannel")).queue();
        }
    }

    /**
     * Handles the auto-complete interaction event.
     *
     * @param event   the auto-complete interaction event
     * @param context the event context
     */
    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        if ("where".equals(event.getFocusedOption().getName())) {
            event.replyChoices(Completion.complete("", "same channel", "custom channel")).queue();
        }
    }
}
