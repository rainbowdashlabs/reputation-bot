/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.locale.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.text.TextFormatting;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handler for the list locales command.
 */
public class List implements SlashHandler {

    /**
     * Creates a new list handler.
     */
    public List(){
    }

    /**
     * Handles the slash command interaction event to list available locales.
     *
     * @param event the SlashCommandInteractionEvent
     * @param context the EventContext
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var languages = context.guildLocalizer().localizer().languages();
        var builder = TextFormatting.getTableBuilder(languages,
                context.localize("words.language"), context.localize("words.code"));
        languages.forEach(lang -> builder.setNextRow(lang.getNativeName(), lang.getLocale()));
        event.reply(context.localize("command.locale.list.message.list") + "\n" + builder).queue();
    }
}
