package de.chojo.repbot.listener;

import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class InternalCommandListener extends ListenerAdapter {
    private final Configuration configuration;

    public InternalCommandListener(Configuration configuration) {
        this.configuration = configuration;
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
            event.getMessage().reply("Starting upgrade. Will be back soon!").complete();
            System.exit(20);
            return;
        }

        if ("restart".equalsIgnoreCase(args[0])) {
            event.getMessage().reply("Restarting. Will be back soon!").complete();
            System.exit(10);
            return;
        }

        if ("shutdown".equalsIgnoreCase(args[0])) {
            event.getMessage().reply("Initializing shutdown. Good bye :c").complete();
            System.exit(0);
        }
    }
}
