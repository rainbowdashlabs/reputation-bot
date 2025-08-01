/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.log.handler;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class BaseAnalyzer {
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context, Reputation reputation) {
        var optMessageId = ValueParser.parseLong(event.getOption("messageid").getAsString());
        if (optMessageId.isEmpty()) {
            event.reply(context.localize("error.invalidMessage")).setEphemeral(true).complete();
            return;
        }

        var resultEntry = reputation.analyzer()
                                    .get(optMessageId.get());

        if (resultEntry.isEmpty()) {
            event.reply(context.localize("command.log.analyzer.notanalyzed")).setEphemeral(true).complete();
            return;
        }

        var embed = resultEntry.get().embed(event.getGuild(), context);

        var reputationLogEntries = reputation.log().messageLog(optMessageId.get(), 10);

        var entries = LogFormatter.mapMessageLogEntry(context.guildLocalizer(), reputationLogEntries);

        var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.log.message.message.log", Replacement.create("ID", optMessageId.get()));

        LogFormatter.buildFields(entries, builder);

        embed.add(builder.build());

        event.replyEmbeds(embed).setEphemeral(true).complete();

    }
}
