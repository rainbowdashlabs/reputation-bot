/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.log.handler;

import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.dao.snapshots.ReputationLogEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

public final class LogFormatter {
    static final int PAGE_SIZE = 15;

    private LogFormatter() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    static List<String> mapUserLogEntry(LocalizationContext context, List<ReputationLogEntry> logEntries, Function<ReputationLogEntry, Long> userId) {
        List<String> entries = new ArrayList<>();
        for (var logEntry : logEntries) {
            var thankType = context.localize(logEntry.type().nameLocaleKey());
            String jumpLink = "";
            if (logEntry.type() != ThankType.COMMAND) {
                jumpLink = createJumpLink(context, logEntry);
            }
            entries.add(String.format("%s **%s** %s %s", logEntry.timestamp(),
                    thankType, User.fromId(userId.apply(logEntry)).getAsMention(), jumpLink));
        }
        return entries;
    }

    public static List<String> mapMessageLogEntry(LocalizationContext context, List<ReputationLogEntry> logEntries) {
        if (logEntries.isEmpty()) return Collections.emptyList();

        List<String> entries = new ArrayList<>();
        for (var logEntry : logEntries) {
            entries.add(formatMessageLogEntry(context, logEntry));
        }
        return entries;
    }

    public static String formatMessageLogEntry(LocalizationContext context, ReputationLogEntry logEntry) {
        String jumpLink = "";
        jumpLink = createJumpLink(context, logEntry);
        var thankType = context.localize(logEntry.type().nameLocaleKey());
        if (logEntry.type() == ThankType.COMMAND) {
            return String.format("%s **%s** %s ➜ %s", logEntry.timestamp(),
                    thankType, User.fromId(logEntry.donorId()).getAsMention(), User.fromId(logEntry.receiverId())
                                                                                   .getAsMention());
        }
        return String.format("%s **%s** %s ➜ %s **|** %s", logEntry.timestamp(),
                thankType, User.fromId(logEntry.donorId()).getAsMention(), User.fromId(logEntry.receiverId())
                                                                               .getAsMention(), jumpLink);
    }

    public static String formatMessageLogEntrySimple(LocalizationContext context, ReputationLogEntry logEntry) {
        var thankType = context.localize(logEntry.type().nameLocaleKey());
        return String.format("%s **%s** %s ➜ %s", logEntry.timestamp(),
                thankType, User.fromId(logEntry.donorId()).getAsMention(), User.fromId(logEntry.receiverId())
                                                                               .getAsMention());
    }

    static String createJumpLink(LocalizationContext context, ReputationLogEntry log) {
        var jump = context.localize("words.link",
                Replacement.create("TARGET", "$words.message$"),
                Replacement.create("URL", log.getMessageJumpLink()));

        String refJump = null;
        if (log.hasRefMessage()) {
            refJump = context.localize("words.link",
                    Replacement.create("TARGET", "$%s$".formatted("words.refMessage")),
                    Replacement.create("URL", log.getRefMessageJumpLink()));
        }

        return String.format("**%s** %s", jump, refJump == null ? "" : "➜ **" + refJump + "**");
    }

    static MessageEditData userLogEmbed(LocalizationContext context, Member user, String title, List<String> log, boolean noPremium) {
        var builder = new LocalizedEmbedBuilder(context)
                .setAuthor(title, null, user.getEffectiveAvatarUrl(), Replacement.create("USER", user.getEffectiveName()));
        if (noPremium) {
            builder.setFooter("supporter.fullreputationlog");
        }
        buildFields(log, builder);
        return MessageEditData.fromEmbeds(builder.build());
    }

    public static void buildFields(List<String> entries, LocalizedEmbedBuilder embedBuilder) {
        var joiner = new StringJoiner("\n");
        for (var entry : entries) {
            if (joiner.length() + entry.length() > MessageEmbed.DESCRIPTION_MAX_LENGTH) break;
            joiner.add(entry);
        }
        embedBuilder.setDescription(joiner.toString());
    }
}
