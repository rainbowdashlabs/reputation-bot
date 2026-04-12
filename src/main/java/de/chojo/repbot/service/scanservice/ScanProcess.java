/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.scanservice;

import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.analyzer.results.AnalyzerResult;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.service.scanservice.scans.Scan;
import de.chojo.repbot.web.pojo.scan.ScanProgress;
import de.chojo.repbot.web.pojo.scan.ScanTarget;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class ScanProcess {
    private static final Logger log = getLogger(ScanProcess.class);
    private final List<GuildChannel> channels;
    private List<? extends Scan> scans;
    private final MessageAnalyzer analyzer;
    private final RepGuild guild;
    private Thread currWorker;
    private Instant start = Instant.now();
    private Instant lastSeen;

    public ScanProcess(MessageAnalyzer analyzer, RepGuild guild, List<GuildChannel> channels) {
        this.analyzer = analyzer;
        this.guild = guild;
        this.channels = channels;
    }

    public void init() {
        scans = channels.stream()
                .map(c -> Scan.create(this, c))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public void scan() {
        scans.stream().filter(c -> !c.done()).findFirst().ifPresent(Scan::scan);

        currWorker = Thread.currentThread();
        lastSeen = Instant.now();
        save();
    }

    public AnalyzerResult analyze(Message message) {
        return analyzer.processMessage(message, settings(), false);
    }

    public Guild guild() {
        return guild.guild();
    }

    public Settings settings() {
        return guild.settings();
    }

    public Reputation reputation() {
        return guild.reputation();
    }

    public boolean done() {
        return scans.stream().allMatch(Scan::done);
    }

    public int maxMessages() {
        return scans.stream().mapToInt(Scan::maxMessages).sum();
    }

    public int scanned() {
        return scans.stream().mapToInt(Scan::scanned).sum();
    }

    public int hits() {
        return scans.stream().mapToInt(Scan::hits).sum();
    }

    public List<GuildChannel> channels() {
        return channels;
    }

    public Instant start() {
        return start;
    }

    public ScanProgress progress() {
        return new ScanProgress(
                ScanTarget.GUILD,
                guild.guild().getIdLong(),
                guild.guild().getName(),
                scanned(),
                maxMessages(),
                hits(),
                scans.stream().map(Scan::progress).toList());
    }

    public void save() {
        guild.scan().saveProgress(progress(), start, done() ? Instant.now() : null);
    }
}
