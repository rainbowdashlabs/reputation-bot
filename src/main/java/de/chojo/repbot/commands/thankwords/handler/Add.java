/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.thankwords.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Handler for the "add thankword" slash command, which adds a thankword pattern.
 */
public class Add implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs an Add handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public Add(Guilds guilds) {
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
        var pattern = event.getOption("pattern").getAsString();
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            event.reply(context.localize("error.invalidRegex"))
                 .setEphemeral(true)
                 .queue();
            return;
        }
        if (guilds.guild(event.getGuild()).settings().thanking().thankwords().add(pattern)) {
            event.reply(context.localize("command.thankwords.add.message.added",
                    Replacement.create("REGEX", pattern, Format.CODE))).queue();
        }
    }
}
