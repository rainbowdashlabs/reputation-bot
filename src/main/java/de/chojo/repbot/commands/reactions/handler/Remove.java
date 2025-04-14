/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reactions.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.regex.Pattern;

/**
 * Handles the removal of reactions in a guild.
 */
public class Remove implements SlashHandler {
    private static final Pattern EMOTE_PATTERN = Pattern.compile("<a?:.*?:(?<id>\\d*?)>");

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
     * Handles the slash command interaction event for removing reactions.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var reactions = guilds.guild(event.getGuild()).settings().thanking().reactions();
        var emote = event.getOption("emote").getAsString();
        var matcher = EMOTE_PATTERN.matcher(emote);
        if (matcher.find()) {
            if (reactions.remove(matcher.group("id"))) {
                event.reply(context.localize("command.reactions.remove.message.removed")).queue();
                return;
            }
            event.reply(context.localize("command.reactions.remove.message.notfound")).setEphemeral(true).queue();
            return;
        }

        if (reactions.remove(emote)) {
            event.reply(context.localize("command.reactions.remove.message.removed")).queue();
            return;
        }
        event.reply(context.localize("command.reactions.remove.message.notfound")).setEphemeral(true).queue();
    }

    /**
     * Handles the auto-complete interaction event for the emote option.
     *
     * @param event the auto-complete interaction event
     * @param context the event context
     */
    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        if ("emote".equals(event.getFocusedOption().getName())) {
            var reactions = guilds.guild(event.getGuild())
                                  .settings()
                                  .thanking()
                                  .reactions()
                                  .reactions()
                                  .stream()
                                  .limit(25)
                                  .map(Choice::toChoice)
                                  .toList();
            event.replyChoices(reactions).queue();
        }
    }
}
