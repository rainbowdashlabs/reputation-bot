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
 * Handler for the max message age command in abuse protection.
 */
public class MaxMessageAge implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a new MaxMessageAge handler with the specified Guilds provider.
     *
     * @param guilds the Guilds provider
     */
    public MaxMessageAge(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event for setting or getting the max message age.
     *
     * @param event the SlashCommandInteractionEvent
     * @param context the EventContext
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guilds.guild(event.getGuild());
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize("command.abuseprotection.message.age.message.get",
                    Replacement.create("MINUTES", abuseSettings.maxMessageAge()))).queue();
            return;
        }
        var age = event.getOption("minutes").getAsInt();

        age = Math.max(0, age);
        event.reply(context.localize("command.abuseprotection.message.age.message.get",
                Replacement.create("MINUTES", abuseSettings.maxMessageAge(age)))).queue();
    }
}
