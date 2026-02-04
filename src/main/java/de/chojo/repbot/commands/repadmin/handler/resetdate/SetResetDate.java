/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repadmin.handler.resetdate;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.Text;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SetResetDate implements SlashHandler {
    private final GuildRepository guildRepository;

    public SetResetDate(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var year = event.getOption("year").getAsInt();
        var month = event.getOption("month").getAsInt();
        var day = event.getOption("day").getAsInt();

        Instant date;
        try {
            date = LocalDate.of(year, month, day).atStartOfDay(ZoneId.of("UTC")).toInstant();
        } catch (DateTimeException e) {
            event.reply(WebPromo.promoString(context) +context.localize("error.invalidDate"))
                 .setEphemeral(true)
                 .complete();
            return;
        }

        guildRepository.guild(event.getGuild()).settings().general().resetDate(date);

        event.reply(WebPromo.promoString(context) +context.localize("command.repadmin.resetdate.set.message.set", Replacement.create("DATE", Text.timestampDateTime(date))))
             .setEphemeral(true)
             .complete();
    }
}
