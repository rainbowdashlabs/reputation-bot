/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repadmin.handler.resetdate;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Handler for the "set reset date" slash command.
 * This command sets the reset date for the guild's settings.
 */
public class SetResetDate implements SlashHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private final Guilds guilds;

    /**
     * Constructs a new SetResetDate handler.
     *
     * @param guilds the guilds provider
     */
    public SetResetDate(Guilds guilds) {
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
        var year = event.getOption("year").getAsInt();
        var month = event.getOption("month").getAsInt();
        var day = event.getOption("day").getAsInt();

        LocalDate date;
        try {
            date = LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            event.reply(context.localize("error.invalidDate")).setEphemeral(true).queue();
            return;
        }

        guilds.guild(event.getGuild()).settings().general().resetDate(date);

        event.reply(context.localize("command.repadmin.resetdate.set.message.set", Replacement.create("DATE", FORMATTER.format(date))))
             .setEphemeral(true).queue();
    }
}
