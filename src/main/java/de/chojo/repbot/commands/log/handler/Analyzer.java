/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.log.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public class Analyzer implements SlashHandler {
    private final Guilds guilds;

    public Analyzer(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optMessageId = ValueParser.parseLong(event.getOption("messageid").getAsString());
        if (optMessageId.isEmpty()) {
            event.reply(context.localize("error.invalidMessage")).setEphemeral(true).queue();
            return;
        }
        var reputation = guilds.guild(event.getGuild()).reputation();
        var resultEntry = reputation.analyzer()
                                    .get(optMessageId.get());

        if (resultEntry.isEmpty()) {
            event.reply(context.localize("command.log.analyzer.notanalyzed")).setEphemeral(true).queue();
            return;
        }

        var embed = resultEntry.get().embed(event.getGuild(), context);

        var reputationLogEntries = reputation.log().messageLog(optMessageId.get(), 10);

        var entries = LogFormatter.mapMessageLogEntry(context, reputationLogEntries);

        var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.log.message.message.log", Replacement.create("ID", optMessageId.get()));

        LogFormatter.buildFields(entries, builder);

        embed.add(builder.build());

        event.replyEmbeds(embed).setEphemeral(true).queue();
    }

    public static void sendAnalyzerLog(IReplyCallback callback, Guilds guilds, long messageId, EventContext context) {
        var reputation = guilds.guild(callback.getGuild()).reputation();
        var resultEntry = reputation.analyzer()
                                    .get(messageId);

        if (resultEntry.isEmpty()) {
            callback.reply(context.localize("command.log.analyzer.notanalyzed")).setEphemeral(true).queue();
            return;
        }

        var embed = resultEntry.get().embed(callback.getGuild(), context);

        var reputationLogEntries = reputation.log().messageLog(messageId, 10);

        var entries = LogFormatter.mapMessageLogEntry(context, reputationLogEntries);

        var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.log.message.message.log", Replacement.create("ID", messageId));

        LogFormatter.buildFields(entries, builder);

        embed.add(builder.build());

        callback.replyEmbeds(embed).setEphemeral(true).queue();
    }
}
