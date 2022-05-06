package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static de.chojo.repbot.commands.Top.registerPage;

public class TopMonth extends SimpleCommand {
    private static final int TOP_PAGE_SIZE = 10;
    private final Guilds guilds;

    public TopMonth(Guilds guilds) {
        super(CommandMeta.builder("topmonth", "command.reputation.description"));
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var ranking = guilds.guild(event.getGuild()).reputation().ranking().month(TOP_PAGE_SIZE);
        registerPage(ranking, event, context);
    }
}
