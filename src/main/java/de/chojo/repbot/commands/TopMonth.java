package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.ReputationData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;

import static de.chojo.repbot.commands.Top.buildTop;

public class TopMonth extends SimpleCommand {
    private static final int TOP_PAGE_SIZE = 10;
    private final ReputationData reputationData;
    private final Localizer loc;

    public TopMonth(DataSource dataSource, Localizer localizer) {
        super("topmonth",
                new String[]{"reptop"},
                "command.reputation.description",
                argsBuilder()
                        .add(OptionType.INTEGER, "page", "page")
                        .build(),
                Permission.UNKNOWN);
        reputationData = new ReputationData(dataSource);
        loc = localizer;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        var page = event.getOption("page");
        var l = page == null ? 1 : page.getAsLong();
        event.replyEmbeds(top(event.getGuild(), (int) l)).queue();
    }

    private MessageEmbed top(Guild guild, int page) {
        var ranking = reputationData.getMonthRanking(guild, TOP_PAGE_SIZE, page);
        return buildTop(ranking, loc, guild);
    }
}
