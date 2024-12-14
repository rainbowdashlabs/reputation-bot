/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.roles.handler.donor;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;

/**
 * Handles the removal of a donor role from a guild.
 */
public class RemoveDonor implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a RemoveDonor handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public RemoveDonor(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event for removing a donor role.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var role = event.getOption("role").getAsRole();
        guilds.guild(event.getGuild()).settings().thanking().donorRoles().remove(role);
        event.reply(context.localize("command.roles.donor.remove.message.remove",
                Replacement.createMention(role))).setAllowedMentions(Collections.emptyList()).queue();
    }
}
