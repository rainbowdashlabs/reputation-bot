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
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handler for the "check" slash command, which checks a message for thankwords.
 */
public class Check implements SlashHandler {
    private final Guilds guilds;
    private final MessageAnalyzer messageAnalyzer;

    /**
     * Constructs a Check handler with the specified guilds provider and message analyzer.
     *
     * @param guilds the guilds provider
     * @param messageAnalyzer the message analyzer
     */
    public Check(Guilds guilds, MessageAnalyzer messageAnalyzer) {
        this.guilds = guilds;
        this.messageAnalyzer = messageAnalyzer;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var settings = guilds.guild(event.getGuild()).settings();
        var guildSettings = settings.thanking().thankwords();
        var messageId = event.getOption("message").getAsString();

        if (!Verifier.isValidId(messageId)) {
            event.reply(context.localize("error.invalidMessage")).queue();
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

    /**
     * Processes the message analysis result and builds the response embed.
     *
     * @param result the match analyzer result
     * @param builder the localized embed builder
     */
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
