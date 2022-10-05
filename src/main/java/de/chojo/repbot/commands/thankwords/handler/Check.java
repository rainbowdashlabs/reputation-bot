package de.chojo.repbot.commands.thankwords.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.analyzer.results.match.MatchResult;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Check implements SlashHandler {
    private final Guilds guilds;
    private final MessageAnalyzer messageAnalyzer;

    public Check(Guilds guilds, MessageAnalyzer messageAnalyzer) {
        this.guilds = guilds;
        this.messageAnalyzer = messageAnalyzer;
    }

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

    private void processMessage(MatchResult result, LocalizedEmbedBuilder builder) {
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
