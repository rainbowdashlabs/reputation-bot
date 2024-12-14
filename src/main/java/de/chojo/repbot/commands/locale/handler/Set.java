/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.locale.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * Handler for setting the locale of a guild.
 */
public class Set implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a new Set handler.
     *
     * @param guilds the guilds provider
     */
    public Set(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command to set the locale.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        DiscordLocale locale = null;
        for (var value : DiscordLocale.values()) {
            if (value.getNativeName().equals(event.getOption("language").getAsString())) {
                locale = value;
            }
        }

        if (locale == null || !context.guildLocalizer().localizer().languages().contains(locale)) {
            event.reply(context.localize("command.locale.set.message.invalidlocale")).setEphemeral(true).queue();
            return;
        }

        if (guilds.guild(event.getGuild()).settings().general().language(locale)) {
            event.reply(context.localize("command.locale.set.message.set",
                    Replacement.create("LOCALE", locale.getNativeName(), Format.CODE))).queue();
        }
    }

    /**
     * Handles the auto-complete interaction for the locale command.
     *
     * @param event the command auto-complete interaction event
     * @param context the event context
     */
    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var option = event.getFocusedOption();
        if ("language".equalsIgnoreCase(option.getName())) {
            event.replyChoices(Completion.complete(option.getValue(), context.guildLocalizer().localizer()
                                                                             .languages(), DiscordLocale::getNativeName))
                 .queue();
        }
    }
}
