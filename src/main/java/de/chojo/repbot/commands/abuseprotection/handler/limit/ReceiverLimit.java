/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.abuseprotection.handler.limit;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ReceiverLimit implements SlashHandler {
    private final GuildRepository guildRepository;

    public ReceiverLimit(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guildRepository.guild(event.getGuild());
        var protection = guild.settings().abuseProtection();
        var limit = event.getOption("limit");
        if (limit != null) {
            protection.maxReceived(limit.getAsInt());
        }

        var hours = event.getOption("hours");
        if (hours != null) {
            protection.maxReceivedHours(hours.getAsInt());
        }

        if (protection.maxReceived() == 0) {
            event.reply(WebPromo.promoString(context) + context.localize("command.abuseprotection.limit.receiver.message.disabled"))
                 .setEphemeral(true)
                 .complete();
            return;
        }
        event.reply(WebPromo.promoString(context) + context.localize("command.abuseprotection.limit.receiver.message.set",
                     Replacement.create("AMOUNT", protection.maxReceived()),
                     Replacement.create("HOURS", protection.maxReceivedHours())))
             .setEphemeral(true)
             .complete();
    }
}
