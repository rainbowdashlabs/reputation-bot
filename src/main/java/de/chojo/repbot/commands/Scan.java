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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
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
    public static final int INTERVAL_MS = 2000;
    private static final int SCAN_THREADS = 10;
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
                subCommandBuilder()
                        .add("start", "command.scan.description", argsBuilder()
                                .add(OptionType.CHANNEL, "channel", "channel")
                                .add(OptionType.INTEGER, "number_messages", "number_messages")
                                .build()
                        )
                        .add("cancel", "command.scan.sub.cancel")
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

        if (context.argsEmpty()) return false;
        var subCmd = context.argString(0).get();
        if ("cancel".equalsIgnoreCase(subCmd)) {
            if (!activeScans.contains(eventWrapper.getGuild().getIdLong())) {
                eventWrapper.replyErrorAndDelete(eventWrapper.localize("command.scan.sub.cancel.noTask"), 10);
                return true;
            }
            cancelScan(eventWrapper.getGuild());
            return true;
        }

        if ("start".equalsIgnoreCase(subCmd)) {
             context = context.subContext(subCmd);
            if (activeScans.contains(eventWrapper.getGuild().getIdLong())) {
                eventWrapper.replyErrorAndDelete(":stop_sign: " + eventWrapper.localize("command.scan.error.running"), 10);
                return true;
            }

            if (activeScans.size() >= SCAN_THREADS) {
                eventWrapper.replyErrorAndDelete(":stop_sign: " + eventWrapper.localize("command.scan.error.queueFull"), 10);
                return true;
            }

            if (context.argsEmpty()) {
                scanChannel(eventWrapper, eventWrapper.getTextChannel(), 30000);
                return true;
            }

            var messages = 30000;
            if (context.argInt(1).isPresent()) {
                messages = Math.max(context.argInt(1).get(), 0);
            }

            var channel = eventWrapper.getTextChannel();
            if (context.argString(0).isPresent()) {
                channel = DiscordResolver.getTextChannel(eventWrapper.getGuild(), context.argString(0).get()).orElse(null);
            }

            if (channel == null) {
                eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidChannel"), 10);
                return true;
            }
            scanChannel(eventWrapper, channel, messages);
        }
        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_HISTORY)) {
            event.reply(loc.localize("command.scan.error.history")).setEphemeral(true).queue();
            return;
        }

        var subCmd = event.getSubcommandName();

        if ("cancel".equalsIgnoreCase(subCmd)) {
            if (!activeScans.contains(event.getGuild().getIdLong())) {
                event.reply(loc.localize("command.scan.sub.cancel.noTask")).setEphemeral(true).queue();
                return;
            }
            event.reply(loc.localize("command.scan.canceling")).queue();
            cancelScan(event.getGuild());
            return;
        }

        if ("start".equalsIgnoreCase(subCmd)) {

            if (activeScans.contains(event.getGuild().getIdLong())) {
                event.reply(":stop_sign: " + loc.localize("command.scan.error.running")).setEphemeral(true).queue();
                return;
            }

            if (activeScans.size() >= SCAN_THREADS) {
                event.reply(":stop_sign: " + loc.localize("command.scan.error.queueFull")).setEphemeral(true).queue();
                return;
            }

            if (event.getOptions().isEmpty()) {
                scanChannel(event, event.getTextChannel(), 30000);
                return;
            }
            var messages = 30000;
            var channel = event.getTextChannel();
            if (event.getOption("number_messages") != null) {
                messages = (int) event.getOption("number_messages").getAsLong();
            }
            if (event.getOption("channel") != null) {
                event.getOption("channel").getAsGuildChannel();
            }
            scanChannel(event, channel, Math.max(messages, 0));
        }
    }

    private void scanChannel(MessageEventWrapper eventWrapper, TextChannel channel, int messageCount) {
        var duration = DurationFormatUtils.formatDuration((long) messageCount / 100 * INTERVAL_MS, "mm:ss");
        eventWrapper.reply(eventWrapper.localize("command.scan.scheduling", Replacement.create("DURATION", duration))).queue();
        preSchedule(channel, messageCount);
    }

    private void scanChannel(SlashCommandEvent event, TextChannel channel, int messageCount) {
        var duration = DurationFormatUtils.formatDuration((long) messageCount / 100 * INTERVAL_MS, "mm:ss");
        event.reply(loc.localize("command.scan.scheduling", event.getGuild(), Replacement.create("DURATION", duration))).queue();
        preSchedule(channel, messageCount);
    }

    private void preSchedule(TextChannel channel, int messageCount) {
        var history = channel.getHistory();

        var guildSettings = guildData.getGuildSettings(channel.getGuild());
        if (guildSettings.isEmpty()) return;
        var pattern = guildSettings.get().getThankwordPattern();

        schedule(history, pattern, channel, messageCount);
    }

    private void schedule(MessageHistory history, Pattern pattern, TextChannel reportChannel, int calls) {
        var loc = this.loc.getContextLocalizer(reportChannel.getGuild());
        var progressMessage = reportChannel.sendMessage(loc.localize("command.scan.progress",
                Replacement.create("PERCENT", String.format("%.02f", 0d))) + " " + TextGenerator.progressBar(0, 40)).complete();
        var scanProcess = new ScanProcess(this.loc, progressMessage, history, pattern, calls, reputationData);

        activeScans.add(reportChannel.getGuild().getIdLong());
        reportChannel.getGuild().loadMembers().get();
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

    public void cancelScan(Guild guild) {
        activeScans.remove(guild.getIdLong());
        cancel.add(guild.getIdLong());
    }

    public void finishScan(ScanProcess scanProcess) {
        activeScans.remove(scanProcess.getGuild().getIdLong());
        finished.add(scanProcess);
    }

    @Getter
    private static class ScanProcess {
        private final Localizer loc;
        private final Guild guild;
        private final TextChannel resultChannel;
        private final Message progressMessage;
        private final MessageHistory history;
        private final Pattern pattern;
        private final int calls;
        private final ReputationData reputationData;
        private int scanned = 0;
        private int hits = 0;
        private int callsLeft;
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
                var result = MessageAnalyzer.processMessage(pattern, message, 0, false, 0.85, 3);

                var donator = result.getDonator();
                var refMessage = result.getReferenceMessage();
                for (var resultReceiver : result.getReceivers()) {
                    switch (result.getType()) {
                        case FUZZY, MENTION, ANSWER -> {
                            reputationData.logReputation(guild, donator, resultReceiver.getReference().getUser(), message, refMessage, result.getType());
                            hit();
                        }
                        case NO_MATCH -> {
                        }
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
}
