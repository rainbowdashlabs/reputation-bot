package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.pagination.bag.PageBag;
import de.chojo.jdautil.pagination.bag.PrivatePageBag;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.GuildRanking;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;
import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Top extends SimpleCommand {
    private static final int TOP_PAGE_SIZE = 10;
    private final ReputationData reputationData;

    public Top(DataSource dataSource) {
        super(CommandMeta.builder("top", "command.reputation.description"));
        reputationData = new ReputationData(dataSource);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var ranking = reputationData.getRanking(event.getGuild(), TOP_PAGE_SIZE);
        registerPage(ranking, event, context);
    }


    public static void registerPage(GuildRanking guildRanking, SlashCommandInteractionEvent event, SlashCommandContext context) {
        context.registerPage(new PageBag(guildRanking.pages()) {
            @Override
            public CompletableFuture<MessageEmbed> buildPage() {
                return CompletableFuture.supplyAsync(() -> {
                    var ranking = guildRanking.page(current());

                    if (ranking.isEmpty()) {
                        return createBaseBuilder(context, event.getGuild())
                                .setDescription("*" + context.localize("command.top.empty") + "*")
                                .build();
                    }

                    var maxRank = ranking.get(ranking.size() - 1).rank();
                    var rankString = ranking.stream().map(rank -> rank.fancyString((int) maxRank)).collect(Collectors.joining("\n"));

                    return createBaseBuilder(context, event.getGuild())
                            .setDescription(rankString)
                            .build();
                });
            }
        }, true);
    }

    private static LocalizedEmbedBuilder createBaseBuilder(SlashCommandContext context, Guild guild) {
        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.top.title", Replacement.create("GUILD", guild.getName()))
                .setColor(Color.CYAN);
    }
}
