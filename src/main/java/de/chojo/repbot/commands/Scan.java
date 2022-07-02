package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ContextLocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.LogNotify;
import de.chojo.repbot.util.PermissionErrorHandler;
import de.chojo.repbot.util.TextGenerator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
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

public class Scan extends SimpleCommand {
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

    public Scan(Guilds guilds, Configuration configuration) {
        super(CommandMeta.builder("scan", "command.scan.description")
                .addSubCommand("start", "command.scan.sub.start", argsBuilder()
                        .add(SimpleArgument.channel("channel", "command.scan.sub.start.arg.numberMessages"))
                        .add(SimpleArgument.integer("number_messages", "command.scan.sub.start.arg.channel")))
                .addSubCommand("cancel", "command.scan.sub.cancel")
                .adminCommand());
        this.guilds = guilds;
        this.configuration = configuration;
        worker.scheduleAtFixedRate(() -> {
            finishTasks();
            finishCanceledTasks();
            checkStuckTasks();
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void checkStuckTasks() {
        for (var activeScan : activeScans) {
            if (activeScan.lastSeen().isAfter(Instant.now().minus(THREAD_MAX_SEEN_SECONDS, ChronoUnit.SECONDS))) {
                continue;
            }
            if (activeScan.interrupt()) {
                log.warn("Scan thread was stuck and interrupted. Scan was canceled on guild {}", activeScan.guild().getIdLong());
                cancelScan(activeScan.guild());
            }
        }
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_HISTORY)) {
            event.reply(context.localize("command.scan.error.history")).setEphemeral(true).queue();
            return;
        }

        var subCmd = event.getSubcommandName();

        if ("cancel".equalsIgnoreCase(subCmd)) {
            if (!isActive(event.getGuild())) {
                event.reply(context.localize("command.scan.sub.cancel.noTask")).setEphemeral(true).queue();
                return;
            }
            event.reply(context.localize("command.scan.canceling")).queue();
            cancelScan(event.getGuild());
            return;
        }

        if ("start".equalsIgnoreCase(subCmd)) {

            if (isActive(event.getGuild())) {
                event.reply(":stop_sign: " + context.localize("command.scan.error.running")).setEphemeral(true).queue();
                return;
            }

            if (activeScans.size() >= SCAN_THREADS) {
                event.reply(":stop_sign: " + context.localize("command.scan.error.queueFull")).setEphemeral(true).queue();
                return;
            }

            if (event.getOptions().isEmpty()) {
                scanChannel(event, context, event.getTextChannel(), 30000);
                return;
            }
            var messages = 30000;
            var channel = event.getTextChannel();
            if (event.getOption("number_messages") != null) {
                messages = (int) event.getOption("number_messages").getAsLong();
            }
            if (event.getOption("channel") != null) {
                var guildChannel = event.getOption("channel").getAsGuildChannel();
                if (guildChannel.getType() != ChannelType.TEXT) {
                    event.reply(context.localize("error.invalidChannel")).queue();
                    return;
                }
                channel = (TextChannel) guildChannel;
            }

            scanChannel(event, context, channel, Math.max(messages, 0));
        }
    }

    private void scanChannel(SlashCommandInteractionEvent event, SlashCommandContext context, TextChannel channel, int messageCount) {
        if (PermissionErrorHandler.assertAndHandle(channel, context.localizer().localizer(), configuration,
                Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY)) {
            return;
        }
        var duration = DurationFormatUtils.formatDuration((long) messageCount / 100 * INTERVAL_MS, "mm:ss");
        event.reply(context.localize("command.scan.scheduling", Replacement.create("DURATION", duration))).queue();
        preSchedule(context, channel, messageCount);
    }

    private void preSchedule(SlashCommandContext context, TextChannel channel, int messageCount) {
        var history = channel.getHistory();
        var pattern = guilds.guild(channel.getGuild()).settings().thanking().thankwords().thankwordPattern();

        schedule(history, context, pattern, channel, messageCount);
    }

    private void schedule(MessageHistory history, SlashCommandContext context, Pattern pattern, TextChannel reportChannel, int calls) {
        var progressMessage = reportChannel.sendMessage(context.localize("command.scan.progress",
                Replacement.create("PERCENT", String.format("%.02f", 0.0d))) + " " + TextGenerator.progressBar(0, 40)).complete();
        var scanProcess = new ScanProcess(messageAnalyzer, context.localizer(), progressMessage, history, pattern, calls, guilds);
        setActive(scanProcess);
        reportChannel.getGuild().loadMembers().get();
        worker.schedule(() -> processScan(scanProcess), 0, TimeUnit.SECONDS);
    }

    private boolean isActive(Guild guild) {
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
        scan.progressMessage().editMessage(scan.loc.localize("command.scan.progress",
                Replacement.create("PERCENT", String.format("%.02f", 100.0d))) + " " + TextGenerator.progressBar(1, 40)).queue();
        var embed = new LocalizedEmbedBuilder(scan.loc)
                .setTitle("command.scan.completed")
                .setDescription("command.scan.result",
                        Replacement.create("SCANNED", scan.scanned()),
                        Replacement.create("HITS", scan.hits()))
                .build();
        scan.resultChannel().sendMessageEmbeds(embed).reference(scan.progressMessage()).queue();
    }

    private void finishCanceledTasks() {
        if (canceled.isEmpty()) return;
        var scan = canceled.poll();
        setInactive(scan);
        var embed = new LocalizedEmbedBuilder(scan.loc)
                .setTitle("command.scan.canceled")
                .setDescription("command.scan.result",
                        Replacement.create("SCANNED", scan.scanned()),
                        Replacement.create("HITS", scan.hits()))
                .build();
        scan.resultChannel().sendMessageEmbeds(embed).reference(scan.progressMessage()).queue();
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

    private static class ScanProcess {
        private final MessageAnalyzer messageAnalyzer;
        private final ContextLocalizer loc;
        private final Guild guild;
        private final TextChannel resultChannel;
        private final Message progressMessage;
        private final MessageHistory history;
        private final Pattern pattern;
        private final int calls;
        private final Guilds guilds;
        // This is the offset of two bot messages of the reputation bot.
        private int scanned = -2;
        private int hits;
        private int callsLeft;
        private long time;
        private Instant lastSeen;
        private Thread currWorker;

        private ScanProcess(MessageAnalyzer messageAnalyzer, ContextLocalizer localizer, Message progressMessage, MessageHistory history, Pattern pattern, int calls, Guilds data) {
            this.messageAnalyzer = messageAnalyzer;
            loc = localizer;
            guild = progressMessage.getGuild();
            resultChannel = progressMessage.getTextChannel();
            this.progressMessage = progressMessage;
            this.history = history;
            this.pattern = pattern;
            // The history will already contain two messages of the bot at this point.
            this.calls = Math.min(Math.max(0, calls + 2), 10000);
            callsLeft = this.calls;
            guilds = data;
        }

        public void countScan() {
            scanned++;
        }

        public void hit() {
            hits++;
        }

        public boolean scan() {
            if (currWorker != null) {
                log.debug("Scanning takes to long. Skipping execution of scan to catch up");
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

                var result = messageAnalyzer.processMessage(pattern, message, null, false, 3);

                var donator = result.donator();
                var refMessage = result.referenceMessage();
                var reputation = guilds.guild(guild).reputation();
                for (var resultReceiver : result.receivers()) {
                    switch (result.type()) {
                        case FUZZY, MENTION, ANSWER -> {
                            if (Verifier.equalSnowflake(donator, resultReceiver.getReference())) continue;
                            if (reputation.user(resultReceiver.getReference().getUser())
                                    .addReputation(donator != null && guild.isMember(donator) ? donator : null, message, refMessage, result.type())) {
                                hit();
                            }
                        }
                        case NO_MATCH -> {
                        }
                    }
                }
            }
            var progress = (calls - Math.max(callsLeft, 0)) / (double) calls;
            var progressString = String.format("%.02f", progress * 100.0d);
            log.debug("Scan progress for guild {}: {}", guild.getIdLong(), progressString);
            progressMessage.editMessage(loc.localize("command.scan.progress",
                    Replacement.create("PERCENT", progressString)) + " " + TextGenerator.progressBar(progress, 40)).complete();
            time = Instant.now().until(start, ChronoUnit.MILLIS);
            currWorker = null;
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
    }
}
