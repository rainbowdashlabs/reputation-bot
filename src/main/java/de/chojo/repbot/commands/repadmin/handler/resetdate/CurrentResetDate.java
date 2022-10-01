package de.chojo.repbot.commands.repadmin.handler.resetdate;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.format.DateTimeFormatter;

public class CurrentResetDate implements SlashHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private final Guilds guilds;

    public CurrentResetDate(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var date = guilds.guild(event.getGuild()).settings().general().resetDate();

        if (date == null) {
            event.reply(context.localize("command.repadmin.resetdate.current.message.notset"))
                 .setEphemeral(true)
                 .queue();
            return;
        }

        event.reply(context.localize("command.repadmin.resetdate.current.message.set", Replacement.create("DATE", FORMATTER.format(date))))
             .setEphemeral(true)
             .queue();
    }
}
