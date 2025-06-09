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
import de.chojo.repbot.dao.provider.GuildRepository;
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

public class ScanProcess {
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
    private final GuildRepository guildRepository;
    // This is the offset of two bot messages of the reputation bot.
    private int scanned = -2;
    private int hits;
    private int callsLeft;
    private long time;
    private Instant lastSeen;
    private Thread currWorker;

    ScanProcess(MessageAnalyzer messageAnalyzer, LocalizationContext localizer, Message progressMessage, MessageHistory history, Pattern pattern, int calls, GuildRepository data) {
        this.messageAnalyzer = messageAnalyzer;
        loc = localizer;
        guild = progressMessage.getGuild();
        resultChannel = progressMessage.getChannel().asTextChannel();
        this.progressMessage = progressMessage;
        this.history = history;
        this.pattern = pattern;
        // The history will already contain two messages of the bot at this point.
        this.calls = Math.min(Math.max(0, calls + 2), MAX_MESSAGES);
        callsLeft = this.calls;
        guildRepository = data;
        lastSeen = Instant.now();
    }

    public void countScan() {
        scanned++;
    }

    public void hit() {
        hits++;
    }

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
            var settings = guildRepository.guild(guild).settings();
            var result = messageAnalyzer.processMessage(pattern, message, settings, false,
                    settings.abuseProtection().maxMessageReputation());

            if (result.isEmpty()) continue;

            var matchResult = result.asMatch();

            var donator = matchResult.donor();
            var reputation = guildRepository.guild(guild).reputation();
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
        lastSeen = Instant.now();
        return callsLeft > 0;
    }

    public boolean interrupt() {
        if (currWorker == null) return false;
        currWorker.interrupt();
        return true;
    }

    public long getTime() {
        return time;
    }

    public Guild guild() {
        return guild;
    }

    public Message progressMessage() {
        return progressMessage;
    }

    public int scanned() {
        return scanned;
    }

    public int hits() {
        return hits;
    }

    public TextChannel resultChannel() {
        return resultChannel;
    }

    public Instant lastSeen() {
        return lastSeen;
    }

    public LocalizationContext loc() {
        return loc;
    }
}
