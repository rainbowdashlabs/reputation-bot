package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
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
import net.dv8tion.jda.api.sharding.ShardManager;
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
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(11);
    public static final int INTERVAL_MS = 2000;
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final Set<Long> activeScans = new HashSet<>();
    private final Set<Long> cancel = new HashSet<>();
    private final Queue<ScanProcess> finished = new ArrayDeque<>();
    private final Queue<ScanProcess> canceled = new ArrayDeque<>();

    public Scan(DataSource dataSource, ShardManager shardManager, Localizer localizer) {
        super("scan",
                null,
                "Scan a channel for reputations.",
                "[channel] -n <number_messages>",
                subCommandBuilder()
                        .add("cancel", null, "Cancel a running scan.")
                        .build(),
                Permission.ADMINISTRATOR);
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        executorService.scheduleAtFixedRate(() -> {
            finishTasks();
            finishCanceledTasks();
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (!eventWrapper.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_HISTORY)) {
            eventWrapper.replyNonMention("Missing permission for message history").queue();
            return true;
        }
        if (context.argsEmpty()) {
            scanChannel(eventWrapper, eventWrapper.getTextChannel(), 30000);
            return true;
        }

        if ("cancel".equalsIgnoreCase(context.argString(0).get())) {
            if (!activeScans.contains(eventWrapper.getGuild().getIdLong())) {
                eventWrapper.replyErrorAndDelete("No task is running for this guild.", 10);
                return true;
            }
            cancelScan(eventWrapper.getGuild());
            return true;
        }

        if (activeScans.contains(eventWrapper.getGuild().getIdLong())) {
            eventWrapper.replyErrorAndDelete(":stop_sign: A task is already running on this guild. Please wait until it is finished.", 10);
            return true;
        }

        if (activeScans.size() >= 10) {
            eventWrapper.replyErrorAndDelete(":stop_sign: Too many scans are running currently. Please wait until a slot is free.", 10);
            return true;
        }


        var messages = 30000;
        if (context.hasFlagValue("n")) {
            var n1 = context.getFlag("n", ValueParser::parseInt);
            if (n1.isEmpty()) {
                eventWrapper.replyErrorAndDelete("Invalid number.", 10);
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
            eventWrapper.replyErrorAndDelete("Invalid channel", 10);
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
        eventWrapper.replyNonMention("Scheduling your scan. The estimated required time until completion is: " + duration + " minutes").queue();

        schedule(history, pattern, eventWrapper, messageCount / 100);
    }

    private void schedule(MessageHistory history, Pattern pattern, MessageEventWrapper eventWrapper, int calls) {

        var progressMessage = eventWrapper.answer("Progress: 0.00%\n" + TextGenerator.progressBar(0, 40)).complete();
        var scanProcess = new ScanProcess(progressMessage, history, pattern, calls, reputationData);

        activeScans.add(eventWrapper.getGuild().getIdLong());

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
        scan.getProgressMessage().editMessage("Progress: 100.00%\n" + TextGenerator.progressBar(1, 40)).queue();
        scan.getResultChannel().sendMessage("Scan completed.\n"
                + "Scanned " + scan.scanned + " Messages.\n"
                + "Added " + scan.hits + " reputations.").reference(scan.getProgressMessage()).queue();
    }

    private void finishCanceledTasks() {
        if (canceled.isEmpty()) return;
        var scan = canceled.poll();
        activeScans.remove(scan.getGuild().getIdLong());
        scan.getResultChannel().sendMessage("Scan canceled.\n"
                + "Scanned " + scan.scanned + " Messages.\n"
                + "Added " + scan.hits + " reputations.").reference(scan.getProgressMessage()).queue();
    }

    @Getter
    private static class ScanProcess {
        private int scanned = 0;
        private int hits = 0;
        private final Guild guild;
        private final TextChannel resultChannel;
        private final Message progressMessage;
        private MessageHistory history;
        private Pattern pattern;
        private final int calls;
        private int callsLeft;
        private ReputationData reputationData;
        private long time;

        public ScanProcess(Message progressMessage, MessageHistory history, Pattern pattern, int calls, ReputationData data) {
            this.guild = progressMessage.getGuild();
            this.resultChannel = progressMessage.getTextChannel();
            this.progressMessage = progressMessage;
            this.history = history;
            this.pattern = pattern;
            this.calls = calls;
            this.callsLeft = calls;
            reputationData = data;
        }

        public void scanned() {
            scanned++;
        }

        public void hit() {
            hits++;
        }

        public boolean scan() {
            var start = Instant.now();
            var size = history.size();
            var messages = history.retrievePast(100).timeout(10, TimeUnit.SECONDS).complete();
            if (size == history.size()) {
                return false;
            }

            for (var message : messages) {
                scanned();
                var result = MessageAnalyzer.processMessage(pattern, message);
                switch (result.getType()) {
                    case FUZZY -> {
                        if (result.getConfidenceScore() < 0.85) continue;
                        reputationData.logReputation(guild, result.getDonator(), result.getReceiver(), message);
                        hit();
                    }
                    case MENTION -> {
                        reputationData.logReputation(guild, result.getDonator(), result.getReceiver(), message);
                        hit();
                    }
                    case ANSWER -> {
                        reputationData.logReputation(guild, result.getDonator(), result.getReceiver(), result.getReferenceMessage());
                        hit();
                    }
                    case NO_MATCH -> {
                    }
                }
            }
            var progress = (calls - callsLeft) / (double) calls;
            progressMessage.editMessage("Progress: " + String.format("%.02f", progress * 100) + "%\n" + TextGenerator.progressBar(progress, 40)).complete();
            callsLeft--;
            time = Instant.now().until(start, ChronoUnit.MILLIS);
            return callsLeft != 0;
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
