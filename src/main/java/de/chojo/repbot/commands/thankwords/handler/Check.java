/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.thankwords.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.analyzer.results.match.MatchAnalyzerResult;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Check implements SlashHandler {
    private final GuildRepository guildRepository;
    private final MessageAnalyzer messageAnalyzer;

    public Check(GuildRepository guildRepository, MessageAnalyzer messageAnalyzer) {
        this.guildRepository = guildRepository;
        this.messageAnalyzer = messageAnalyzer;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var settings = guildRepository.guild(event.getGuild()).settings();
        var guildSettings = settings.thanking().thankwords();
        var messageId = event.getOption("message").getAsString();

        if (!Verifier.isValidId(messageId)) {
            event.reply(context.localize("error.invalidMessage")).complete();
            return;
        }

        var message = event.getChannel().retrieveMessageById(messageId).complete();
        var result = messageAnalyzer.processMessage(guildSettings.thankwordPattern(), message, settings, true, settings.abuseProtection()
                                                                                                                       .maxMessageReputation());
        if (result.isEmpty()) {
            event.reply(context.localize("command.thankwords.check.message.noMatch")).queue();
            return;
        }

        var builder = new LocalizedEmbedBuilder(context.guildLocalizer());
        processMessage(result.asMatch(), builder);
        event.replyEmbeds(builder.build()).queue();
    }

    private void processMessage(MatchAnalyzerResult result, LocalizedEmbedBuilder builder) {
        if (result.thankType() == ThankType.FUZZY) {
            for (var receiver : result.asFuzzy().weightedReceiver()) {
                builder.addField("command.thankwords.check.message.fuzzy",
                        "$%s$%n$%s$".formatted("command.thankwords.check.message.result", "command.thankwords.check.message.confidence"),
                        false, Replacement.create("DONATOR", result.donor().getAsMention()),
                        Replacement.create("RECEIVER", receiver.getReference().getAsMention()),
                        Replacement.create("SCORE", String.format("%.3f", receiver.getWeight())));

            }
        } else {
            for (var receiver : result.receivers()) {
                switch (result.thankType()) {
                    case MENTION -> builder.addField("command.thankwords.check.message.mention",
                            "command.thankwords.check.message.result",
                            false, Replacement.create("DONATOR", result.donor().getAsMention()),
                            Replacement.create("RECEIVER", receiver.getAsMention()));
                    case ANSWER -> builder.addField("command.thankwords.check.message.answer",
                            "$%s$%n$%s$".formatted("command.thankwords.check.message.result", "command.thankwords.check.message.reference"),
                            false, Replacement.create("URL", result.asAnswer().referenceMessage().getJumpUrl()),
                            Replacement.create("DONATOR", result.donor().getAsMention()),
                            Replacement.create("RECEIVER", receiver.getAsMention()));
                }
            }
        }
    }
}
