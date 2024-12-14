/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.scan.util;

import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.Text;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Class representing a scan process for messages in a channel.
 */
public class ScanProcess {
    /**
     * The maximum number of messages to scan.
     */
    public static final int MAX_MESSAGES = 10000;
    private static final Logger log = getLogger(ScanProcess.class);
    private final MessageAnalyzer messageAnalyzer;
    private final LocalizationContext loc;
    private final Guild guild;
    private final TextChannel resultChannel;
    private final Message progressMessage;
    private final MessageHistory history;
    private final Pattern pattern;
    private final int calls;
    private final Guilds guilds;
    private int scanned = -2;
    private int hits;
    private int callsLeft;
    private long time;
    private Instant lastSeen;
    private Thread currWorker;

    /**
     * Constructs a new ScanProcess.
     *
     * @param messageAnalyzer the message analyzer
     * @param localizer the localization context
     * @param progressMessage the progress message
     * @param history the message history
     * @param pattern the pattern to match
     * @param calls the number of calls to make
     * @param data the guilds provider
     */
    ScanProcess(MessageAnalyzer messageAnalyzer, LocalizationContext localizer, Message progressMessage, MessageHistory history, Pattern pattern, int calls, Guilds data) {
        this.messageAnalyzer = messageAnalyzer;
        loc = localizer;
        guild = progressMessage.getGuild();
        resultChannel = progressMessage.getChannel().asTextChannel();
        this.progressMessage = progressMessage;
        this.history = history;
        this.pattern = pattern;
        this.calls = Math.min(Math.max(0, calls + 2), MAX_MESSAGES);
        callsLeft = this.calls;
        guilds = data;
    }

    /**
     * Increments the scanned message count.
     */
    public void countScan() {
        scanned++;
    }

    /**
     * Increments the hit count.
     */
    public void hit() {
        hits++;
    }

    /**
     * Executes the scan process.
     *
     * @return true if there are more messages to scan, false otherwise
     */
    public boolean scan() {
        if (currWorker != null) {
            log.debug("Scanning takes too long. Skipping execution of scan to catch up");
            return false;
        }

        currWorker = Thread.currentThread();
        lastSeen = Instant.now();
        if (callsLeft == 0) {
            currWorker = null;
            return false;
        }
        var start = Instant.now();
        var size = history.size();
        var messages = history.retrievePast(Math.min(callsLeft, 100)).timeout(30, TimeUnit.SECONDS).complete();
        callsLeft -= Math.min(callsLeft, 100);
        if (size == history.size()) {
            currWorker = null;
            return false;
        }

        for (var message : messages) {
            countScan();

            if (message.getAuthor().isBot()) continue;
            var settings = guilds.guild(guild).settings();
            var result = messageAnalyzer.processMessage(pattern, message, settings, false,
                    settings.abuseProtection().maxMessageReputation());

            if (result.isEmpty()) continue;

            var matchResult = result.asMatch();

            var donator = matchResult.donor();
            var reputation = guilds.guild(guild).reputation();
            for (var resultReceiver : matchResult.receivers()) {
                if (Verifier.equalSnowflake(donator, resultReceiver)) continue;

                switch (matchResult.thankType()) {
                    case FUZZY, MENTION -> {
                        if (reputation.user(resultReceiver.getUser())
                                      .addOldReputation(donator != null && guild.isMember(donator) ? donator : null,
                                              message, null, matchResult.thankType())) {
                            hit();
                        }
                    }
                    case ANSWER -> {
                        if (reputation.user(resultReceiver.getUser())
                                      .addOldReputation(donator != null && guild.isMember(donator) ? donator : null,
                                              message, matchResult.asAnswer().referenceMessage(), matchResult.thankType())) {
                            hit();
                        }

                    }
                }
            }
        }
        var progress = (calls - Math.max(callsLeft, 0)) / (double) calls;
        var progressString = String.format("%.02f", progress * 100.0d);
        log.debug("Scan progress for guild {}: {}", guild.getIdLong(), progressString);
        progressMessage.editMessage("```ANSI\n" +
                loc.localize("command.scan.scanner.message.progress",
                        Replacement.create("PERCENT", progressString)) + " " + Text.progressBar(progress, 40) +
                "```").complete();
        time = Instant.now().until(start, ChronoUnit.MILLIS);
        currWorker = null;
        return callsLeft > 0;
    }

    /**
     * Interrupts the current scan process.
     *
     * @return true if the scan process was interrupted, false otherwise
     */
    public boolean interrupt() {
        if (currWorker == null) return false;
        currWorker.interrupt();
        return true;
    }

    /**
     * Retrieves the time taken for the scan.
     *
     * @return the time taken for the scan
     */
    public long getTime() {
        return time;
    }

    /**
     * Retrieves the guild associated with the scan.
     *
     * @return the guild
     */
    public Guild guild() {
        return guild;
    }

    /**
     * Retrieves the progress message.
     *
     * @return the progress message
     */
    public Message progressMessage() {
        return progressMessage;
    }

    /**
     * Retrieves the number of scanned messages.
     *
     * @return the number of scanned messages
     */
    public int scanned() {
        return scanned;
    }

    /**
     * Retrieves the number of hits.
     *
     * @return the number of hits
     */
    public int hits() {
        return hits;
    }

    /**
     * Retrieves the result channel.
     *
     * @return the result channel
     */
    public TextChannel resultChannel() {
        return resultChannel;
    }

    /**
     * Retrieves the last seen time.
     *
     * @return the last seen time
     */
    public Instant lastSeen() {
        return lastSeen;
    }

    /**
     * Retrieves the localization context.
     *
     * @return the localization context
     */
    public LocalizationContext loc() {
        return loc;
    }
}
