package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.ReputationUser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;
import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

public class Top extends SimpleCommand {
    private static final int TOP_PAGE_SIZE = 10;
    private final ReputationData reputationData;
    private final Localizer loc;

    public Top(DataSource dataSource, Localizer localizer) {
        super("top",
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
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var page = event.getOption("page");
        var l = page == null ? 1 : page.getAsLong();
        event.replyEmbeds(top(event.getGuild(), (int) Math.max(1, l))).queue();
    }

    private MessageEmbed top(Guild guild, int page) {
        var ranking = reputationData.getRanking(guild, TOP_PAGE_SIZE, page);
        return buildTop(ranking, loc, guild);
    }

    public static MessageEmbed buildTop(List<ReputationUser> ranking, ILocalizer loc, Guild guild) {
        if(ranking.isEmpty()) {
            return createBaseBuilder(loc, guild)
                    .setDescription("*" + loc.localize("command.top.empty", guild) + "*")
                    .build();
        }

        var maxRank = ranking.get(ranking.size() - 1).rank();
        var rankString = ranking.stream().map(rank -> rank.fancyString((int) maxRank)).collect(Collectors.joining("\n"));

        return createBaseBuilder(loc, guild)
                .setDescription(rankString)
                .build();
    }

    private static LocalizedEmbedBuilder createBaseBuilder(ILocalizer loc, Guild guild) {
        return new LocalizedEmbedBuilder(loc, guild)
                .setTitle(loc.localize("command.top.title", guild,
                        Replacement.create("GUILD", guild.getName())))
                .setColor(Color.CYAN);
    }
}
