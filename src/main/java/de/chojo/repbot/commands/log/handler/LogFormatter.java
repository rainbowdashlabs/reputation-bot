package de.chojo.repbot.commands.log.handler;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.snapshots.ReputationLogEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

final class LogFormatter {
    static final int PAGE_SIZE = 15;

    private LogFormatter() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    static List<String> mapUserLogEntry(EventContext context, List<ReputationLogEntry> logEntries, Function<ReputationLogEntry, Long> userId) {
        List<String> entries = new ArrayList<>();
        for (var logEntry : logEntries) {
            var thankType = context.localize(logEntry.type().localeKey());
            var jumpLink = createJumpLink(context, logEntry);
            entries.add(String.format("%s **%s** %s %s", logEntry.timestamp(),
                    thankType, User.fromId(userId.apply(logEntry)).getAsMention(), jumpLink));
        }
        return entries;
    }

    static List<String> mapMessageLogEntry(EventContext context, List<ReputationLogEntry> logEntries) {
        if (logEntries.isEmpty()) return Collections.emptyList();

        List<String> entries = new ArrayList<>();
        for (var logEntry : logEntries) {
            var jumpLink = createJumpLink(context, logEntry);
            var thankType = context.localize(logEntry.type().localeKey());
            entries.add(String.format("%s **%s** %s ➜ %s **|** %s", logEntry.timestamp(),
                    thankType, User.fromId(logEntry.donorId()).getAsMention(), User.fromId(logEntry.receiverId()).getAsMention(), jumpLink));
        }
        return entries;
    }

    static String createJumpLink(EventContext context, ReputationLogEntry log) {
        var jump = context.localize("words.link",
                Replacement.create("TARGET", "$words.message$"),
                Replacement.create("URL", log.getMessageJumpLink()));

        String refJump = null;
        if (log.hasRefMessage()) {
            refJump = context.localize("words.link",
                    Replacement.create("TARGET", "$%s$".formatted("words.refMessage")),
                    Replacement.create("URL", log.getMessageJumpLink()));
        }

        return String.format("**%s** %s", jump, refJump == null ? "" : "➜ **" + refJump + "**");
    }

    static MessageEmbed userLogEmbed(EventContext context, Member user, String title, List<String> log) {
        var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setAuthor(title, null, user.getEffectiveAvatarUrl(), Replacement.create("USER", user.getEffectiveName()));
        buildFields(log, builder);
        return builder.build();
    }
    static void buildFields(List<String> entries, LocalizedEmbedBuilder embedBuilder) {
        var joiner = new StringJoiner("\n");
        for (var entry : entries) {
            if (joiner.length() + entry.length() > MessageEmbed.DESCRIPTION_MAX_LENGTH) break;
            joiner.add(entry);
        }
        embedBuilder.setDescription(joiner.toString());
    }
}
