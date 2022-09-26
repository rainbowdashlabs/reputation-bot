package de.chojo.repbot.commands.bot.handler.system;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.util.LogNotify;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Restart implements SlashHandler {
    private static final Logger log = getLogger(Restart.class);
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        log.info(LogNotify.STATUS, "Restart command received from {}. Attempting restart.", event.getUser().getAsTag());
        event.reply("Restarting. Will be back soon!").complete();
        System.exit(10);
    }
}
