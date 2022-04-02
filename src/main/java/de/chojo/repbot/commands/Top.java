package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.ReputationUser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;
import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

public class Top extends SimpleCommand {
    private static final int TOP_PAGE_SIZE = 10;
    private final ReputationData reputationData;

    public Top(DataSource dataSource) {
        super(CommandMeta.builder("top","command.reputation.description")
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
        var ranking = reputationData.getRanking(guild, TOP_PAGE_SIZE, page);
        return buildTop(ranking, context, guild);
    }

    public static MessageEmbed buildTop(List<ReputationUser> ranking, SlashCommandContext context, Guild guild) {
        if(ranking.isEmpty()) {
            return createBaseBuilder(context, guild)
                    .setDescription("*" + context.localize("command.top.empty") + "*")
                    .build();
        }

        var maxRank = ranking.get(ranking.size() - 1).rank();
        var rankString = ranking.stream().map(rank -> rank.fancyString((int) maxRank)).collect(Collectors.joining("\n"));

        return createBaseBuilder(context, guild)
                .setDescription(rankString)
                .build();
    }

    private static LocalizedEmbedBuilder createBaseBuilder(SlashCommandContext context, Guild guild) {
        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.top.title", Replacement.create("GUILD", guild.getName()))
                .setColor(Color.CYAN);
    }
}
