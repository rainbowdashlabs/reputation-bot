package de.chojo.repbot.commands.bot.handler.system;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.util.LogNotify;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Shudown implements SlashHandler {
    private static final Logger log = getLogger(Shudown.class);

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        log.info(LogNotify.STATUS, "Shutdown command received from {}. Shutting down.", event.getUser().getAsTag());
        event.reply("Initializing shutdown. Good bye :c").complete();
        System.exit(0);

    }
}
