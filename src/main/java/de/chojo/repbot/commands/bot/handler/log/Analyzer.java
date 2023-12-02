package de.chojo.repbot.commands.bot.handler.log;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.log.handler.BaseAnalyzer;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Analyzer extends BaseAnalyzer implements SlashHandler {
    private final Guilds guilds;

    public Analyzer(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild_id = ValueParser.parseLong(event.getOption("guild_id").getAsString());
        onSlashCommand(event, context, guilds.byId(guild_id.get()).reputation());
    }
}
