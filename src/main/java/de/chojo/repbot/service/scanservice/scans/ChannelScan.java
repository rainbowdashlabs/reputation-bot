/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.scanservice.scans;

import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.service.scanservice.ScanProcess;
import de.chojo.repbot.web.pojo.scan.ScanProgress;
import de.chojo.repbot.web.pojo.scan.ScanTarget;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.Permission.*;
import static org.slf4j.LoggerFactory.getLogger;

public class ChannelScan implements Scan {
    private static final Logger log = getLogger(ChannelScan.class);
    public static final int MAX_MESSAGES = 10000;
    private final ScanProcess process;
    private final GuildMessageChannel channel;
    private final MessageHistory history;
    private int scanned = 0;
    private int hits = 0;
    private boolean done;

    private ChannelScan(ScanProcess process, GuildMessageChannel channel) {
        this.process = process;
        this.channel = channel;
        history = channel.getHistory();
    }

    public static ChannelScan create(ScanProcess process, GuildMessageChannel channel) {
        return new ChannelScan(process, channel);
    }

    @Override
    public void scan() {
        if (!process.guild().getSelfMember().hasPermission(channel, MESSAGE_HISTORY, VIEW_CHANNEL)) {
            done = true;
            return;
        }
        List<Message> messages;
        try {
            messages = history.retrievePast(100).timeout(30, TimeUnit.SECONDS).complete();
        } catch (Exception e) {
            log.error("Error while scanning channel", e);
            done = true;
            return;
        }

        if (messages.isEmpty()) {
            done = true;
            return;
        }

        for (Message message : messages) {
            try {
                check(message);
            } catch (Exception e) {
                log.error("Error processing message", e);
            }
        }
    }

    private void check(Message message) {
        countScan();

        if (message.getAuthor().isBot()) return;
        var result = process.analyze(message);

        if (result.isEmpty()) return;

        var matchResult = result.asMatch();

        var donator = matchResult.donor();
        for (var resultReceiver : matchResult.receivers()) {
            if (resultReceiver == null) continue;
            if (Verifier.equalSnowflake(donator, resultReceiver)) continue;

            switch (matchResult.thankType()) {
                case FUZZY, MENTION -> {
                    if (process.reputation()
                            .user(resultReceiver.getUser())
                            .addOldReputation(
                                    donator != null && process.guild().isMember(donator) ? donator : null,
                                    message,
                                    null,
                                    matchResult.thankType())) {
                        hit();
                    }
                }
                case ANSWER -> {
                    if (process.reputation()
                            .user(resultReceiver.getUser())
                            .addOldReputation(
                                    donator != null && process.guild().isMember(donator) ? donator : null,
                                    message,
                                    matchResult.asAnswer().referenceMessage(),
                                    matchResult.thankType())) {
                        hit();
                    }
                }
            }
        }
    }

    public void countScan() {
        scanned++;
    }

    public void hit() {
        hits++;
    }

    @Override
    public boolean done() {
        return done || scanned >= MAX_MESSAGES;
    }

    @Override
    public int hits() {
        return hits;
    }

    @Override
    public int scanned() {
        return scanned;
    }

    @Override
    public List<Scan> scans() {
        return List.of(this);
    }

    @Override
    public int maxMessages() {
        return done() ? scanned() : MAX_MESSAGES;
    }

    @Override
    public ScanProgress progress() {
        return new ScanProgress(
                ScanTarget.fromChannelType(channel.getType()),
                channel.getIdLong(),
                channel.getName(),
                scanned(),
                maxMessages(),
                hits(),
                List.of());
    }
}
