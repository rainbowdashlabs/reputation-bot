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
 * Handles the cooldown limit for abuse protection commands.
 */
public class Cooldown implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a Cooldown handler with the specified guild provider.
     *
     * @param guilds the guild provider
     */
    public Cooldown(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event for setting or getting the cooldown limit.
     *
     * @param event   the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guilds.guild(event.getGuild());
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize("command.abuseprotection.limit.cooldown.message.get",
                    Replacement.create("MINUTES", abuseSettings.cooldown()))).queue();
            return;
        }
        var cooldown = event.getOption("minutes").getAsLong();

        event.reply(context.localize("command.abuseprotection.limit.cooldown.message.set",
                Replacement.create("MINUTES", abuseSettings.cooldown((int) cooldown)))).queue();
    }
}
