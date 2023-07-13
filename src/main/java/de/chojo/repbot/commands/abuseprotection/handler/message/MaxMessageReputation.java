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

public class MaxMessageReputation implements SlashHandler {
    private final Guilds guilds;

    public MaxMessageReputation(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guilds.guild(event.getGuild());
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize("command.abuseprotection.message.reputation.message.get",
                    Replacement.create("VALUE", abuseSettings.maxMessageReputation()))).queue();
            return;
        }
        var maxRep = event.getOption("amount").getAsInt();

        maxRep = Math.max(1, maxRep);
        event.reply(context.localize("command.abuseprotection.message.reputation.message.get",
                Replacement.create("VALUE", abuseSettings.maxMessageReputation(maxRep)))).queue();
    }
}
