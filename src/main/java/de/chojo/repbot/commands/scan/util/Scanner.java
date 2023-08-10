/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.scan.util;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.commands.scan.Scan;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.LogNotify;
import de.chojo.repbot.util.PermissionErrorHandler;
import de.chojo.repbot.util.Text;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

public class Scanner {
    public static final int INTERVAL_MS = 2000;
    private static final int SCAN_THREADS = 10;
    private static final long THREAD_MAX_SEEN_SECONDS = 30L;
    private static final Logger log = getLogger(Scan.class);
    private final ThreadGroup scanner = new ThreadGroup("Scanner");
    private final ScheduledExecutorService worker = Executors.newScheduledThreadPool(SCAN_THREADS + 1,
            runnable -> {
                var thread = new Thread(scanner, runnable);
                thread.setUncaughtExceptionHandler((thr, err) -> log.error("Unhandled exception in Scanner Thread {}.", thr.getId(), err));
                return thread;
            });
    private final Guilds guilds;
    private final Set<ScanProcess> activeScans = new HashSet<>();
    private final Set<Long> cancel = new HashSet<>();
    private final Queue<ScanProcess> finished = new ArrayDeque<>();
    private final Queue<ScanProcess> canceled = new ArrayDeque<>();
    private final Configuration configuration;
    private MessageAnalyzer messageAnalyzer;

    public Scanner(Guilds guilds, Configuration configuration) {
        this.guilds = guilds;
        this.configuration = configuration;
        worker.scheduleAtFixedRate(() -> {
            finishTasks();
            finishCanceledTasks();
            checkStuckTasks();
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void scanChannel(SlashCommandInteractionEvent event, EventContext context, TextChannel channel, int messageCount) {
        if (PermissionErrorHandler.assertAndHandle(channel, context.guildLocalizer(), configuration,
                Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY)) {
            return;
        }
        var duration = DurationFormatUtils.formatDuration((long) messageCount / 100 * INTERVAL_MS, "mm:ss");
        event.reply(context.localize("command.scan.scanner.message.scheduling", Replacement.create("DURATION", duration)))
             .queue();
        preSchedule(context, channel, messageCount);
    }

    private void preSchedule(EventContext context, TextChannel channel, int messageCount) {
        var history = channel.getHistory();
        var pattern = guilds.guild(channel.getGuild()).settings().thanking().thankwords().thankwordPattern();

        schedule(history, context, pattern, channel, messageCount);
    }

    private void schedule(MessageHistory history, EventContext context, Pattern pattern, TextChannel reportChannel, int calls) {
        var progressMessage = reportChannel.sendMessage(context.localize("command.scan.scanner.message.progress",
                                                   Replacement.create("PERCENT", String.format("%.02f", 0.0d))) + " " + Text.progressBar(0, 40))
                                           .complete();
        var scanProcess = new ScanProcess(messageAnalyzer, context.guildLocalizer(), progressMessage, history, pattern, calls, guilds);
        setActive(scanProcess);
        reportChannel.getGuild().loadMembers().get();
        worker.schedule(() -> processScan(scanProcess), 0, TimeUnit.SECONDS);
    }

    public boolean isActive(Guild guild) {
        return activeScans.stream().anyMatch(p -> p.guild().getIdLong() == guild.getIdLong());
    }

    public void setActive(ScanProcess process) {
        activeScans.add(process);
    }

    public void setInactive(ScanProcess process) {
        activeScans.remove(process);
    }

    public void setInactive(Guild guild) {
        activeScans.removeIf(p -> p.guild().getIdLong() == guild.getIdLong());
    }

    private void processScan(ScanProcess scan) {
        if (cancel.remove(scan.guild().getIdLong())) {
            canceled.add(scan);
            return;
        }

        // TODO: Remove once all issues seem to be resolved
        boolean scanResult;
        try {
            scanResult = scan.scan();
        } catch (Exception e) {
            log.error(LogNotify.NOTIFY_ADMIN, "Critical error while scanning", e);
            finishScan(scan);
            return;
        }

        if (scanResult) {
            worker.schedule(() -> processScan(scan), Math.max(0, INTERVAL_MS - scan.getTime()), TimeUnit.MILLISECONDS);
        } else {
            finishScan(scan);
        }
    }

    private void finishTasks() {
        if (finished.isEmpty()) return;
        var scan = finished.poll();
        setInactive(scan);
        scan.progressMessage().editMessage(scan.loc().localize("command.scan.scanner.message.progress",
                Replacement.create("PERCENT", String.format("%.02f", 100.0d))) + " " + Text.progressBar(1, 40)).queue();
        var embed = new LocalizedEmbedBuilder(scan.loc())
                .setTitle("command.scan.scanner.message.completed")
                .setDescription("command.scan.scanner.message.result",
                        Replacement.create("SCANNED", scan.scanned()),
                        Replacement.create("HITS", scan.hits()))
                .build();
        scan.resultChannel().sendMessageEmbeds(embed).setMessageReference(scan.progressMessage()).queue();
    }

    private void finishCanceledTasks() {
        if (canceled.isEmpty()) return;
        var scan = canceled.poll();
        setInactive(scan);
        var embed = new LocalizedEmbedBuilder(scan.loc())
                .setTitle("command.scan.scanner.message.canceled")
                .setDescription("command.scan.scanner.message.result",
                        Replacement.create("SCANNED", scan.scanned()),
                        Replacement.create("HITS", scan.hits()))
                .build();
        scan.resultChannel().sendMessageEmbeds(embed).setMessageReference(scan.progressMessage()).queue();
    }

    public boolean isRunning(Guild guild) {
        return isActive(guild);
    }

    public void cancelScan(Guild guild) {
        setInactive(guild);
        cancel.add(guild.getIdLong());
    }

    public void finishScan(ScanProcess scanProcess) {
        setInactive(scanProcess);
        finished.add(scanProcess);
    }

    public void lateInit(MessageAnalyzer messageAnalyzer) {
        this.messageAnalyzer = messageAnalyzer;
    }

    private void checkStuckTasks() {
        for (var activeScan : activeScans) {
            if (activeScan.lastSeen().isAfter(Instant.now().minus(THREAD_MAX_SEEN_SECONDS, ChronoUnit.SECONDS))) {
                continue;
            }
            if (activeScan.interrupt()) {
                log.warn("Scan thread was stuck and interrupted. Scan was canceled on guild {}", activeScan.guild()
                                                                                                           .getIdLong());
                cancelScan(activeScan.guild());
            }
        }
    }

    public boolean limitReached() {
        return activeScans.size() >= SCAN_THREADS;
    }
}
