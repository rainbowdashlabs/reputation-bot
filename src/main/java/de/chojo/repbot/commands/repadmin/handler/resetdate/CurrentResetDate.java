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

import java.time.ZoneId;
import java.time.temporal.ChronoField;

public class CurrentResetDate implements SlashHandler {
    private final GuildRepository guildRepository;

    public CurrentResetDate(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var instant = guildRepository.guild(event.getGuild()).settings().general().resetDate();

        if (instant == null) {
            event.reply(WebPromo.promoString(context) + context.localize("command.repadmin.resetdate.current.message.notset"))
                 .setEphemeral(true)
                 .complete();
            return;
        }

        var zoned = instant.atZone(ZoneId.of("UTC"));
        String date;
        if (zoned.get(ChronoField.SECOND_OF_DAY) == 0 && zoned.get(ChronoField.MILLI_OF_SECOND) == 0) {
            date = Text.timestampDate(instant);
        } else {
            date = Text.timestampDateTime(instant);
        }

        event.reply(WebPromo.promoString(context) +context.localize("command.repadmin.resetdate.current.message.set", Replacement.create("DATE", date)))
             .setEphemeral(true)
             .complete();
    }
}
