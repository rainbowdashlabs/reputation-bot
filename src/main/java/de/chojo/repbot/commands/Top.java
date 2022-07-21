package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.pagination.bag.PageBag;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.pagination.GuildRanking;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Top extends SimpleCommand {
    private static final int TOP_PAGE_SIZE = 10;
    private final Guilds guilds;

    public Top(Guilds guilds) {
        super(CommandMeta.builder("top", "command.reputation.description")
                .addArgument(SimpleArgument.string("mode", "command.reputation.description.arg.mode").withAutoComplete()));
        this.guilds = guilds;
    }

    public static void registerPage(GuildRanking guildRanking, SlashCommandInteractionEvent event, SlashCommandContext context) {
        context.registerPage(new PageBag(guildRanking.pages()) {
            @Override
            public CompletableFuture<MessageEmbed> buildPage() {
                return CompletableFuture.supplyAsync(() -> {
                    var ranking = guildRanking.page(current());

                    var maxRank = ranking.get(ranking.size() - 1).rank();
                    var rankString = ranking.stream().map(rank -> rank.fancyString((int) maxRank)).collect(Collectors.joining("\n"));

                    return createBaseBuilder(guildRanking, context, event.getGuild())
                            .setDescription(rankString)
                            .build();
                });
            }

            @Override
            public CompletableFuture<MessageEmbed> buildEmptyPage() {
                return CompletableFuture.completedFuture(createBaseBuilder(guildRanking, context, event.getGuild())
                        .setDescription("*" + context.localize("command.top.empty") + "*")
                        .build());
            }
        }, true);
    }

    private static LocalizedEmbedBuilder createBaseBuilder(GuildRanking guildRanking, SlashCommandContext context, Guild guild) {
        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle(guildRanking.title(), Replacement.create("GUILD", guild.getName()))
                .setColor(Color.CYAN);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var guild = guilds.guild(event.getGuild());
        var reputationMode = guild.settings().general().reputationMode();
        if (event.getOption("mode") != null) {
            var mode = event.getOption("mode").getAsString();
            reputationMode = switch (mode) {
                case "total" -> ReputationMode.TOTAL;
                case "7 days" -> ReputationMode.ROLLING_WEEK;
                case "30 days" -> ReputationMode.ROLLING_MONTH;
                default -> throw new IllegalStateException("Unexpected value: " + mode);
            };
        }

        var ranking = guild.reputation().ranking().byMode(reputationMode, TOP_PAGE_SIZE);
        registerPage(ranking, event, context);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, SlashCommandContext slashCommandContext) {
        var option = event.getFocusedOption();
        if ("mode".equalsIgnoreCase(option.getName())) {
            event.replyChoices(Completion.complete(option.getValue(), "total", "7 days", "30 days")).queue();
        }
    }
}
