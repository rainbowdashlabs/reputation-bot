package de.chojo.repbot.commands.bot.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Guilds;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Locale;
import java.util.stream.Collectors;

public class Search implements SlashHandler {
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var term = event.getOption("term").getAsString().toLowerCase(Locale.ROOT);

        event.deferReply(true).queue();

        var guilds = event.getJDA().getShardManager().getGuildCache()
                          .stream().filter(guild -> guild.getName().toLowerCase(Locale.ROOT).contains(term))
                          .map(Guilds::prettyName)
                          .collect(Collectors.joining("\n"));

        event.getHook().editOriginal(guilds).queue();
    }
}
