package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.ReputationData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;

import static de.chojo.repbot.commands.Top.registerPage;

public class TopMonth extends SimpleCommand {
    private static final int TOP_PAGE_SIZE = 10;
    private final ReputationData reputationData;

    public TopMonth(DataSource dataSource) {
        super(CommandMeta.builder("topmonth","command.reputation.description"));
        reputationData = new ReputationData(dataSource);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var ranking = reputationData.getMonthRanking(event.getGuild(), TOP_PAGE_SIZE);
        registerPage(ranking, event, context);
    }
}
