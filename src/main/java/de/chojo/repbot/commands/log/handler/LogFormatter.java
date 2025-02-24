/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.log.handler;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
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

/**
 * Utility class for formatting log entries.
 */
public final class LogFormatter {
    static final int PAGE_SIZE = 15;

    /**
     * Private constructor to prevent instantiation.
     * Throws an UnsupportedOperationException if called.
     */
    private LogFormatter() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Maps a list of ReputationLogEntry to a list of formatted log entry strings for a user.
     *
     * @param context the EventContext for localization
     * @param logEntries the list of ReputationLogEntry to map
     * @param userId a function to extract the user ID from a ReputationLogEntry
     * @return a list of formatted log entry strings
     */
    static List<String> mapUserLogEntry(EventContext context, List<ReputationLogEntry> logEntries, Function<ReputationLogEntry, Long> userId) {
        List<String> entries = new ArrayList<>();
        for (var logEntry : logEntries) {
            var thankType = context.localize(logEntry.type().nameLocaleKey());
            var jumpLink = createJumpLink(context, logEntry);
            entries.add(String.format("%s **%s** %s %s", logEntry.timestamp(),
                    thankType, User.fromId(userId.apply(logEntry)).getAsMention(), jumpLink));
        }
        return entries;
    }

    /**
     * Maps a list of ReputationLogEntry to a list of formatted log entry strings for messages.
     *
     * @param context the EventContext for localization
     * @param logEntries the list of ReputationLogEntry to map
     * @return a list of formatted log entry strings
     */
    public static List<String> mapMessageLogEntry(EventContext context, List<ReputationLogEntry> logEntries) {
        if (logEntries.isEmpty()) return Collections.emptyList();

        List<String> entries = new ArrayList<>();
        for (var logEntry : logEntries) {
            var jumpLink = createJumpLink(context, logEntry);
            var thankType = context.localize(logEntry.type().nameLocaleKey());
            entries.add(String.format("%s **%s** %s ➜ %s **|** %s", logEntry.timestamp(),
                    thankType, User.fromId(logEntry.donorId()).getAsMention(), User.fromId(logEntry.receiverId())
                                                                                   .getAsMention(), jumpLink));
        }
        return entries;
    }

    /**
     * Creates a jump link for a given ReputationLogEntry.
     *
     * @param context the EventContext for localization
     * @param log the ReputationLogEntry to create a jump link for
     * @return a formatted jump link string
     */
    static String createJumpLink(EventContext context, ReputationLogEntry log) {
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

    /**
     * Creates a MessageEditData object containing an embed with the user's log entries.
     *
     * @param context the EventContext for localization
     * @param user the Member whose log entries are being displayed
     * @param title the title of the embed
     * @param log the list of log entry strings
     * @return a MessageEditData object containing the embed
     */
    static MessageEditData userLogEmbed(EventContext context, Member user, String title, List<String> log) {
        var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setAuthor(title, null, user.getEffectiveAvatarUrl(), Replacement.create("USER", user.getEffectiveName()));
        buildFields(log, builder);
        return MessageEditData.fromEmbeds(builder.build());
    }

    /**
     * Builds the fields of an embed with the given log entries.
     *
     * @param entries the list of log entry strings
     * @param embedBuilder the LocalizedEmbedBuilder to build the fields on
     */
    public static void buildFields(List<String> entries, LocalizedEmbedBuilder embedBuilder) {
        var joiner = new StringJoiner("\n");
        for (var entry : entries) {
            if (joiner.length() + entry.length() > MessageEmbed.DESCRIPTION_MAX_LENGTH) break;
            joiner.add(entry);
        }
        embedBuilder.setDescription(joiner.toString());
    }
}
