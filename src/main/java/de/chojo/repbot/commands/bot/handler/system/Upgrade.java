package de.chojo.repbot.commands.bot.handler.system;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.util.LogNotify;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Upgrade implements SlashHandler {
    private static final Logger log = getLogger(Upgrade.class);
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        log.info(LogNotify.STATUS, "Upgrade command received from {}. Attempting upgrade.", event.getUser().getAsTag());
        event.reply("Starting upgrade. Will be back soon!").complete();
        System.exit(20);
    }
}
