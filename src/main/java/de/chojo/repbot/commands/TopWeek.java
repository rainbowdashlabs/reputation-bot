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

import static de.chojo.repbot.commands.Top.buildTop;

public class TopWeek extends SimpleCommand {
    private static final int TOP_PAGE_SIZE = 10;
    private final ReputationData reputationData;

    public TopWeek(DataSource dataSource) {
        super(CommandMeta.builder("topweek","command.reputation.description")
                        .addArgument(SimpleArgument.integer("page", "page")));
        reputationData = new ReputationData(dataSource);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var page = event.getOption("page");
        var l = page == null ? 1 : page.getAsLong();
        event.replyEmbeds(top(context, event.getGuild(), (int) Math.max(1, l))).queue();
    }

    private MessageEmbed top(SlashCommandContext context, Guild guild, int page) {
        var ranking = reputationData.getWeekRanking(guild, TOP_PAGE_SIZE, page);
        return buildTop(ranking, context, guild);
    }
}
