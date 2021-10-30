package de.chojo.repbot.listener;

import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.statistic.Statistic;
import de.chojo.repbot.util.LogNotify;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Arrays;

import static org.slf4j.LoggerFactory.getLogger;

public class InternalCommandListener extends ListenerAdapter {
    private static final Logger log = getLogger(InternalCommandListener.class);
    private final Configuration configuration;
    private final Statistic statistic;

    public InternalCommandListener(Configuration configuration, Statistic statistic) {
        this.configuration = configuration;
        this.statistic = statistic;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
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
    }
}
