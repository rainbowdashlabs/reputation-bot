/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.abuseprotection.handler.message;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class MaxMessageAge implements SlashHandler {
    private final GuildRepository guildRepository;

    public MaxMessageAge(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guildRepository.guild(event.getGuild());
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(WebPromo.promoString(context)  + context.localize("command.abuseprotection.message.age.message.get",
                    Replacement.create("MINUTES", abuseSettings.maxMessageAge())))
                 .setEphemeral(true)
                 .complete();
            return;
        }
        var age = event.getOption("minutes").getAsInt();
        age = Math.max(0, age);
        event.reply(WebPromo.promoString(context) + context.localize("command.abuseprotection.message.age.message.get",
                Replacement.create("MINUTES", abuseSettings.maxMessageAge(age))))
             .setEphemeral(true)
             .complete();
    }
}
