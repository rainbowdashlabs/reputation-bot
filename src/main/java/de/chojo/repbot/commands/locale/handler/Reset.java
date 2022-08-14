package de.chojo.repbot.commands.locale.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Reset implements SlashHandler {
    private final Guilds guilds;

    public Reset(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        guilds.guild(event.getGuild()).settings().general().language(null);
        event.reply("command.locale.reset.message.changed").queue();
    }
}
