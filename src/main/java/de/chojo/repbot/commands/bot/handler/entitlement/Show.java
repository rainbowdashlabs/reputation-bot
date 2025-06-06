/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler.entitlement;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;
import java.util.stream.Collectors;

public class Show implements SlashHandler {
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent slash, EventContext eventContext) {
        Long guildId = slash.getOption("guild_id", OptionMapping::getAsLong);
        if (guildId == null) {
            guildId = slash.getGuild().getIdLong();
        }

        List<Entitlement> complete = slash.getJDA().retrieveEntitlements().guild(guildId).excludeEnded(true).complete();
        String collect = complete.stream()
                                 .map(ent -> "SKU: %s\nType: %s\nTill: %s (Consumed: %s)\nEntitlement Id: %s".formatted(ent.getSkuId(), ent.getType(), ent.getTimeEnding(), ent.isConsumed(), ent.getId()))
                                 .collect(Collectors.joining());
        slash.reply(collect).queue();
    }
}
