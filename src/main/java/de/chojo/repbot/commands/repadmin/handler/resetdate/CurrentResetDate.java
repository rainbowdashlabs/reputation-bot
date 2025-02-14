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

import java.time.format.DateTimeFormatter;

/**
 * Handles the slash command for retrieving the current reset date.
 */
public class CurrentResetDate implements SlashHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private final Guilds guilds;

    /**
     * Constructs a CurrentResetDate handler with the specified guild provider.
     *
     * @param guilds the guild provider
     */
    public CurrentResetDate(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event to get the current reset date.
     *
     * @param event   the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var date = guilds.guild(event.getGuild()).settings().general().resetDate();

        if (date == null) {
            event.reply(context.localize("command.repadmin.resetdate.current.message.notset"))
                 .setEphemeral(true)
                 .queue();
            return;
        }

        event.reply(context.localize("command.repadmin.resetdate.current.message.set", Replacement.create("DATE", FORMATTER.format(date))))
             .setEphemeral(true)
             .queue();
    }
}
