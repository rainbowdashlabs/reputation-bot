/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.log.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public class Analyzer extends BaseAnalyzer implements SlashHandler  {
    private final GuildRepository guildRepository;

    public Analyzer(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        onSlashCommand(event, context, guildRepository.guild(event.getGuild()).reputation());
    }

    public static void sendAnalyzerLog(IReplyCallback callback, GuildRepository guildRepository, long messageId, EventContext context) {
        var reputation = guildRepository.guild(callback.getGuild()).reputation();
        var resultEntry = reputation.analyzer()
                                    .get(messageId);

        if (resultEntry.isEmpty()) {
            callback.reply(context.localize("command.log.analyzer.notanalyzed")).setEphemeral(true).complete();
            return;
        }

        var embed = resultEntry.get().embed(callback.getGuild(), context);

        var reputationLogEntries = reputation.log().messageLog(messageId, 10);

        var entries = LogFormatter.mapMessageLogEntry(context.guildLocalizer(), reputationLogEntries);

        var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.log.message.message.log", Replacement.create("ID", messageId));

        LogFormatter.buildFields(entries, builder);

        embed.add(builder.build());

        callback.replyEmbeds(embed).setEphemeral(true).complete();
    }
}
