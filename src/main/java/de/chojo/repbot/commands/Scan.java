package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.util.TextGenerator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.sql.DataSource;
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

@Slf4j
public class Scan extends SimpleCommand {
    private static final int SCAN_THREADS = 10;
    public static final int INTERVAL_MS = 2000;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(SCAN_THREADS + 1);
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final Localizer loc;
    private final Set<Long> activeScans = new HashSet<>();
    private final Set<Long> cancel = new HashSet<>();
    private final Queue<ScanProcess> finished = new ArrayDeque<>();
    private final Queue<ScanProcess> canceled = new ArrayDeque<>();

    public Scan(DataSource dataSource, Localizer localizer) {
        super("scan",
                null,
                "command.scan.description",
                "[channel] [-n <number_messages>]",
                subCommandBuilder()
                        .add("cancel", null, "command.scan.sub.cancel")
                        .build(),
                Permission.MANAGE_SERVER);
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        loc = localizer;
        executorService.scheduleAtFixedRate(() -> {
            finishTasks();
            finishCanceledTasks();
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (!eventWrapper.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_HISTORY)) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("command.scan.error.history"), 10);
            return true;
        }
        if (context.argsEmpty()) {
            scanChannel(eventWrapper, eventWrapper.getTextChannel(), 30000);
            return true;
        }

        if ("cancel".equalsIgnoreCase(context.argString(0).get())) {
            if (!activeScans.contains(eventWrapper.getGuild().getIdLong())) {
                eventWrapper.replyErrorAndDelete(eventWrapper.localize("command.scan.sub.cancel.noTask"), 10);
                return true;
            }
            cancelScan(eventWrapper.getGuild());
            return true;
        }

        if (activeScans.contains(eventWrapper.getGuild().getIdLong())) {
            eventWrapper.replyErrorAndDelete(":stop_sign: " + eventWrapper.localize("command.scan.error.running"), 10);
            return true;
        }

        if (activeScans.size() >= SCAN_THREADS) {
            eventWrapper.replyErrorAndDelete(":stop_sign: " + eventWrapper.localize("command.scan.error.queueFull"), 10);
            return true;
        }


        var messages = 30000;
        if (context.hasFlagValue("n")) {
            var n1 = context.getFlag("n", ValueParser::parseInt);
            if (n1.isEmpty()) {
                eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidNumber"), 10);
                return true;
            }
            messages = Math.max(n1.get(), 0);
        }

        var channel = context.argString(0).get();
        if (channel.equalsIgnoreCase("-n")) {
            scanChannel(eventWrapper, eventWrapper.getTextChannel(), messages);
            return true;
        }

        var textChannel = DiscordResolver.getTextChannel(eventWrapper.getGuild(), channel);
        if (textChannel.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidChannel"), 10);
            return true;
        }
        scanChannel(eventWrapper, textChannel.get(), messages);
        return true;
    }

    private void scanChannel(MessageEventWrapper eventWrapper, TextChannel channel, int messageCount) {
        var history = channel.getHistory();

        var guildSettings = guildData.getGuildSettings(eventWrapper.getGuild());
        if (guildSettings.isEmpty()) return;
        var pattern = guildSettings.get().getThankwordPattern();


        var duration = DurationFormatUtils.formatDuration((long) messageCount / 100 * INTERVAL_MS, "mm:ss");
        eventWrapper.reply(eventWrapper.localize("command.scan.scheduling", Replacement.create("DURATION", duration))).queue();

        schedule(history, pattern, eventWrapper, messageCount);
    }

    private void schedule(MessageHistory history, Pattern pattern, MessageEventWrapper eventWrapper, int calls) {
        var progressMessage = eventWrapper.answer(eventWrapper.localize("command.scan.progress",
                Replacement.create("PERCENT", String.format("%.02f", 0d))) + " " + TextGenerator.progressBar(0, 40)).complete();
        var scanProcess = new ScanProcess(loc, progressMessage, history, pattern, calls, reputationData);

        activeScans.add(eventWrapper.getGuild().getIdLong());

        eventWrapper.getGuild().loadMembers().get();

        executorService.schedule(() -> processScan(scanProcess), 0, TimeUnit.SECONDS);

    }

    private void processScan(ScanProcess scan) {
        if (cancel.remove(scan.getGuild().getIdLong())) {
            canceled.add(scan);
            return;
        }
        if (scan.scan()) {
            executorService.schedule(() -> processScan(scan), Math.max(0, INTERVAL_MS - scan.getTime()), TimeUnit.MILLISECONDS);
        } else {
            finishScan(scan);
        }
    }

    private void finishTasks() {
        if (finished.isEmpty()) return;
        var scan = finished.poll();
        activeScans.remove(scan.getGuild().getIdLong());
        scan.getProgressMessage().editMessage(loc.localize("command.scan.progress", scan.getGuild(),
                Replacement.create("PERCENT", String.format("%.02f", 100d))) + " " + TextGenerator.progressBar(1, 40)).queue();
        var embed = new LocalizedEmbedBuilder(loc, scan.getGuild())
                .setTitle("command.scan.completed")
                .setDescription(loc.localize("command.scan.result", scan.getGuild(),
                        Replacement.create("SCANNED", scan.getScanned()),
                        Replacement.create("HITS", scan.getHits())))
                .build();
        scan.getResultChannel().sendMessage(embed).reference(scan.getProgressMessage()).queue();
    }

    private void finishCanceledTasks() {
        if (canceled.isEmpty()) return;
        var scan = canceled.poll();
        activeScans.remove(scan.getGuild().getIdLong());
        var embed = new LocalizedEmbedBuilder(loc, scan.getGuild())
                .setTitle("command.scan.canceled")
                .setDescription(loc.localize("command.scan.result", scan.getGuild(),
                        Replacement.create("SCANNED", scan.getScanned()),
                        Replacement.create("HITS", scan.getHits())))
                .build();
        scan.getResultChannel().sendMessage(embed).reference(scan.getProgressMessage()).queue();
    }

    public boolean isRunning(Guild guild) {
        return activeScans.contains(guild.getIdLong());
    }

    @Getter
    private static class ScanProcess {
        private int scanned = 0;
        private int hits = 0;
        private final Localizer loc;
        private final Guild guild;
        private final TextChannel resultChannel;
        private final Message progressMessage;
        private final MessageHistory history;
        private final Pattern pattern;
        private final int calls;
        private int callsLeft;
        private final ReputationData reputationData;
        private long time;

        public ScanProcess(Localizer localizer, Message progressMessage, MessageHistory history, Pattern pattern, int calls, ReputationData data) {
            loc = localizer;
            this.guild = progressMessage.getGuild();
            this.resultChannel = progressMessage.getTextChannel();
            this.progressMessage = progressMessage;
            this.history = history;
            this.pattern = pattern;
            this.calls = Math.min(Math.max(0, calls), 100000);
            this.callsLeft = this.calls;
            reputationData = data;
        }

        public void scanned() {
            scanned++;
        }

        public void hit() {
            hits++;
        }

        public boolean scan() {
            if (callsLeft == 0) return false;
            var start = Instant.now();
            var size = history.size();
            var messages = history.retrievePast(Math.min(callsLeft, 100)).timeout(10, TimeUnit.SECONDS).complete();
            callsLeft -= Math.min(callsLeft, 100);
            if (size == history.size()) {
                return false;
            }

            for (var message : messages) {
                scanned();
                var result = MessageAnalyzer.processMessage(pattern, message, 0, false);

                var donator = result.getDonator();
                var receiver = result.getReceivers();
                var refMessage = result.getReferenceMessage();
                switch (result.getType()) {
                    case FUZZY -> {
                        if (result.getConfidenceScore() < 0.85) continue;
                        reputationData.logReputation(guild, donator, receiver, message, refMessage, result.getType());
                        hit();
                    }
                    case MENTION, ANSWER -> {
                        reputationData.logReputation(guild, donator, receiver, message, refMessage, result.getType());
                        hit();
                    }
                    case NO_MATCH -> {
                    }
                }
            }
            var progress = (calls - Math.max(callsLeft, 0)) / (double) calls;
            progressMessage.editMessage(loc.localize("command.scan.progress", guild,
                    Replacement.create("PERCENT", String.format("%.02f", progress * 100d))) + " " + TextGenerator.progressBar(progress, 40)).complete();
            time = Instant.now().until(start, ChronoUnit.MILLIS);
            return callsLeft > 0;
        }

        public long getTime() {
            return time;
        }
    }

    public void cancelScan(Guild guild) {
        activeScans.remove(guild.getIdLong());
        cancel.add(guild.getIdLong());
    }

    public void finishScan(ScanProcess scanProcess) {
        activeScans.remove(scanProcess.getGuild().getIdLong());
        finished.add(scanProcess);
    }
}
