package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.ReputationData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;

import static de.chojo.repbot.commands.Top.registerPage;

public class TopWeek extends SimpleCommand {
    private static final int TOP_PAGE_SIZE = 10;
    private final ReputationData reputationData;

    public TopWeek(DataSource dataSource) {
        super(CommandMeta.builder("topweek", "command.reputation.description"));
        reputationData = new ReputationData(dataSource);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var ranking = reputationData.getWeekRanking(event.getGuild(), TOP_PAGE_SIZE);
        registerPage(ranking, event, context);
    }
}
