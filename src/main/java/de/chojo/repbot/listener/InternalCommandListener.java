package de.chojo.repbot.listener;

import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.statistic.Statistic;
import de.chojo.repbot.util.LogNotify;
import de.chojo.repbot.util.Text;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Arrays;

import static org.slf4j.LoggerFactory.getLogger;

public class InternalCommandListener extends ListenerAdapter {
    private static final Logger log = getLogger(InternalCommandListener.class);
    private final Configuration configuration;
    private final Statistic statistic;
    private final Metrics metrics;

    public InternalCommandListener(Configuration configuration, Statistic statistic, Metrics metrics) {
        this.configuration = configuration;
        this.statistic = statistic;
        this.metrics = metrics;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!configuration.baseSettings().isOwner(event.getAuthor().getIdLong())) return;
        var args = event.getMessage().getContentRaw().replaceAll("\\s+", " ").split(" ");
        if (args.length < 2) return;
        var idRaw = Verifier.getIdRaw(args[0]);
        if (idRaw.isEmpty()) return;
        if (!idRaw.get().equals(event.getJDA().getSelfUser().getId())) return;

        args = Arrays.copyOfRange(args, 1, args.length);

        if ("upgrade".equalsIgnoreCase(args[0])) {
            log.info(LogNotify.STATUS, "Upgrade command received. Attempting upgrade.");
            event.getMessage().reply("Starting upgrade. Will be back soon!").complete();
            System.exit(20);
            return;
        }

        if ("restart".equalsIgnoreCase(args[0])) {
            log.info(LogNotify.STATUS, "Restart command received. Attempting restart.");
            event.getMessage().reply("Restarting. Will be back soon!").complete();
            System.exit(10);
            return;
        }

        if ("shutdown".equalsIgnoreCase(args[0])) {
            log.info(LogNotify.STATUS, "Shutdown command received. Shutting down.");
            event.getMessage().reply("Initializing shutdown. Good bye :c").complete();
            System.exit(0);
        }

        if ("stats".equalsIgnoreCase(args[0]) || "system".equalsIgnoreCase(args[0])) {
            var builder = new EmbedBuilder();
            var systemStatistic = statistic.getSystemStatistic();
            systemStatistic.appendTo(builder);
            event.getMessage().replyEmbeds(builder.build()).queue();
        }

        if ("metrics".equalsIgnoreCase(args[0])) {
            var reply = event.getMessage()
                    .reply("Metrics");

            var commands = metrics.commands().week(1).join();
            if (!commands.commands().isEmpty()) {
                reply.addFile(commands.getChart("Command statistic for week " + Text.month(commands.date())), "commands.png");
            }

            var counts = metrics.reputation().week(1, 24).join();
            if (!counts.stats().isEmpty()) {
                reply.addFile(counts.getChart("Reputation counts per week"), "reputatation.png");
            }

            counts = metrics.reputation().totalWeek(1, 24).join();
            if (!counts.stats().isEmpty()) {
                reply.addFile(counts.getChart("Total reputation"), "total_reputation.png");
            }

            var dows = metrics.reputation().dowMonth(1).join();
            if (!counts.stats().isEmpty()) {
                reply.addFile(dows.getChart("Day of Week stats reputation"), "dow_reputation.png");
            }

            counts = metrics.messages().week(1, 24).join();
            if (!counts.stats().isEmpty()) {
                reply.addFile(counts.getChart("Analyzed Messages per week"), "messages_week.png");
            }

            counts = metrics.messages().day(2, 24 * 7).join();
            if (!counts.stats().isEmpty()) {
                reply.addFile(counts.getChart("Analyzed Messages per day"), "messages_day.png");
            }

            var users = metrics.users().week(1, 24).join();
            if (!users.stats().isEmpty()) {
                reply.addFile(users.getChart("Active users per week"), "users.png");
            }

            reply.queue();
        }
    }
}
