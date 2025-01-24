/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repadmin.handler.resetdate;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handles the removal of the reset date for a guild.
 */
public class RemoveResetDate implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a RemoveResetDate handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public RemoveResetDate(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event for removing the reset date.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        guilds.guild(event.getGuild()).settings().general().resetDate(null);

        event.reply(context.localize("command.repadmin.resetdate.remove.message.removed")).setEphemeral(true).queue();
    }
}
