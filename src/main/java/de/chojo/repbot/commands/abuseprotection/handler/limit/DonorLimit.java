/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.abuseprotection.handler.limit;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handler for the donor limit slash command.
 */
public class DonorLimit implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a new DonorLimit instance.
     *
     * @param guilds the guilds provider
     */
    public DonorLimit(Guilds guilds) {
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
        var guild = guilds.guild(event.getGuild());
        var protection = guild.settings().abuseProtection();
        var limit = event.getOption("limit");
        if (limit != null) {
            protection.maxGiven(limit.getAsInt());
        }

        var hours = event.getOption("hours");
        if (hours != null) {
            protection.maxGivenHours(hours.getAsInt());
        }

        if (protection.maxGiven() == 0) {
            event.reply(context.localize("command.abuseprotection.limit.donor.message.disabled")).setEphemeral(true)
                 .queue();
            return;
        }

        event.reply(context.localize("command.abuseprotection.limit.donor.message.set",
                     Replacement.create("AMOUNT", protection.maxGiven()),
                     Replacement.create("HOURS", protection.maxGivenHours())))
             .setEphemeral(true)
             .queue();
    }
}
