/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.abuseprotection.handler.message;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handles the minimum messages command for abuse protection.
 */
public class MinMessages implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a MinMessages handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public MinMessages(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event for setting or getting the minimum messages for abuse protection.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guilds.guild(event.getGuild());
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize("command.abuseprotection.message.min.message.get",
                    Replacement.create("AMOUNT", abuseSettings.minMessages()))).queue();
            return;
        }
        var minMessages = event.getOption("messages").getAsLong();

        minMessages = Math.max(0, Math.min(minMessages, 100));
        event.reply(context.localize("command.abuseprotection.message.min.message.get",
                Replacement.create("AMOUNT", abuseSettings.minMessages((int) minMessages)))).queue();
    }
}
