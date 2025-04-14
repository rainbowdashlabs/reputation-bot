/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.thankwords.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Handles the removal of thank words from a guild's settings.
 */
public class Remove implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a Remove handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public Remove(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event for removing a thank word pattern.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var pattern = event.getOption("pattern").getAsString();
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            event.reply(context.localize("error.invalidRegex"))
                 .setEphemeral(true)
                 .queue();
            return;
        }
        if (guilds.guild(event.getGuild()).settings().thanking().thankwords().remove(pattern)) {
            event.reply(context.localize("command.thankwords.remove.message.removed",
                    Replacement.create("PATTERN", pattern, Format.CODE))).queue();
            return;
        }
        event.reply(context.localize("command.thankwords.remove.message.patternnotfound"))
             .setEphemeral(true)
             .queue();
    }

    /**
     * Handles the auto-complete interaction event for thank word patterns.
     *
     * @param event the auto-complete interaction event
     * @param context the event context
     */
    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var option = event.getFocusedOption();
        var thankwords = guilds.guild(event.getGuild()).settings().thanking().thankwords().words();
        event.replyChoices(Completion.complete(option.getValue(), thankwords)).queue();
    }
}
