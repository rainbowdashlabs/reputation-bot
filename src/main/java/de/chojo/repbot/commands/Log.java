package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.ReputationLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

public class Log extends SimpleCommand {
    private final ReputationData reputationData;

    public Log(DataSource dataSource) {
        super(CommandMeta.builder("log", "command.log.description")
                .addSubCommand("received", "command.log.sub.received", argsBuilder()
                        .add(SimpleArgument.user("user", "user").asRequired())
                        .add(SimpleArgument.integer("count", "count")))
                .addSubCommand("donated", "command.log.sub.donated", argsBuilder()
                        .add(SimpleArgument.user("user", "user").asRequired())
                        .add(SimpleArgument.integer("count", "count")))
                .addSubCommand("message", "command.log.sub.message", argsBuilder()
                        .add(SimpleArgument.string("message_id", "message_id").asRequired()))
                .withPermission());
        reputationData = new ReputationData(dataSource);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var cmd = event.getSubcommandName();
        if ("received".equalsIgnoreCase(cmd)) {
            received(event, context, event.getOption("user").getAsUser());
        }
        if ("donated".equalsIgnoreCase(cmd)) {
            donated(event, context, event.getOption("user").getAsUser());
        }
        if ("message".equalsIgnoreCase(cmd)) {
            message(event, context);
        }
    }

    private MessageEmbed getMessageLog(SlashCommandContext context, Guild guild, long messageId) {
        var messageLog = reputationData.getMessageLog(messageId, guild, 50);

        var log = mapMessageLogEntry(context, messageLog);

        var builder = new LocalizedEmbedBuilder(context.localizer())
                .setAuthor("command.log.messageLog", Replacement.create("ID", messageId));
        buildFields(log, builder);
        return builder.build();
    }

    private void buildFields(List<String> entries, LocalizedEmbedBuilder embedBuilder) {
        var joiner = new StringJoiner("\n");
        for (var entry : entries) {
            if (joiner.length() + entry.length() > MessageEmbed.DESCRIPTION_MAX_LENGTH) break;
            joiner.add(entry);
        }
        embedBuilder.setDescription(joiner.toString());
    }

    private void message(SlashCommandInteractionEvent event, SlashCommandContext context) {
        event.getOption("message_id");
        var optMessageId = ValueParser.parseLong(event.getOption("message_id").getAsString());
        if (optMessageId.isEmpty()) {
            event.reply(context.localize("error.invalidMessage")).setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(getMessageLog(context, event.getGuild(), event.getOption("message_id").getAsLong())).queue();
    }

    private MessageEmbed sendUserLog(SlashCommandContext context, User user, String title, List<String> log) {
        var builder = new LocalizedEmbedBuilder(context.localizer())
                .setAuthor(title, null, user.getEffectiveAvatarUrl(), Replacement.create("USER", user.getAsTag()));
        buildFields(log, builder);
        return builder.build();
    }

    private void donated(SlashCommandInteractionEvent event, SlashCommandContext context, User user) {
        var limit = Optional.ofNullable(event.getOption("count")).map(OptionMapping::getAsLong).orElse(10L);
        event.reply(wrap(sendUserLog(context, user, "command.log.donatedLog",
                getDonatedLog(context, user, event.getGuild(), limit.intValue())))).queue();
    }

    private List<String> getDonatedLog(SlashCommandContext context, User user, Guild guild, int limit) {
        var userDonatedLog = reputationData.getUserDonatedLog(user, guild, Math.max(5, Math.min(limit, 50)));
        return mapUserLogEntry(context, userDonatedLog, ReputationLogEntry::receiverId);
    }

    private void received(SlashCommandInteractionEvent event, SlashCommandContext context, User user) {
        var limit = Optional.ofNullable(event.getOption("count")).map(OptionMapping::getAsLong).orElse(10L);
        event.reply(wrap(sendUserLog(context, user, "command.log.receivedLog",
                getReceivedLog(user, context, event.getGuild(), limit.intValue())))).queue();
    }

    private List<String> getReceivedLog(User user, SlashCommandContext context, Guild guild, int limit) {
        var userDonatedLog = reputationData.getUserReceivedLog(user, guild, Math.max(5, Math.min(limit, 50)));
        return mapUserLogEntry(context, userDonatedLog, ReputationLogEntry::donorId);
    }

    private List<String> mapUserLogEntry(SlashCommandContext context, List<ReputationLogEntry> logEntries, Function<ReputationLogEntry, Long> userId) {
        List<String> entries = new ArrayList<>();
        for (var logEntry : logEntries) {
            var thankType = context.localize("thankType." + logEntry.type().name().toLowerCase(Locale.ROOT));
            var jumpLink = createJumpLink(context, logEntry);
            entries.add(String.format("**%s** %s %s",
                    thankType, User.fromId(userId.apply(logEntry)).getAsMention(), jumpLink));
        }
        return entries;
    }

    private List<String> mapMessageLogEntry(SlashCommandContext context, List<ReputationLogEntry> logEntries) {
        if (logEntries.isEmpty()) return Collections.emptyList();

        List<String> entries = new ArrayList<>();
        for (var logEntry : logEntries) {
            var jumpLink = createJumpLink(context, logEntry);
            var thankType = context.localize("thankType." + logEntry.type().name().toLowerCase(Locale.ROOT));
            entries.add(String.format("**%s** %s ➜ %s **|** %s",
                    thankType, User.fromId(logEntry.donorId()).getAsMention(), User.fromId(logEntry.receiverId()).getAsMention(), jumpLink));
        }
        return entries;
    }

    private String createJumpLink(SlashCommandContext context, ReputationLogEntry log) {
        var jump = context.localize("words.link",
                Replacement.create("TARGET", "$words.message$"),
                Replacement.create("URL", log.getMessageJumpLink()));

        String refJump = null;
        if (log.hasRefMessage()) {
            refJump = context.localize("words.link",
                    Replacement.create("TARGET", "$words.refMessage$"),
                    Replacement.create("URL", log.getMessageJumpLink()));
        }

        return String.format("**%s** %s", jump, refJump == null ? "" : "➜ **" + refJump + "**");
    }
}
