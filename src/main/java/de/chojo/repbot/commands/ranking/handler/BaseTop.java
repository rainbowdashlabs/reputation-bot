/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.ranking.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.pagination.bag.PageBag;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.util.Premium;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.pagination.Ranking;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.snapshots.RankingEntry;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class BaseTop implements SlashHandler {
    protected static final int TOP_PAGE_SIZE = 10;
    private final GuildRepository guildRepository;
    private final Configuration configuration;
    private final boolean premium;

    protected BaseTop(GuildRepository guildRepository, Configuration configuration, boolean premium) {
        this.guildRepository = guildRepository;
        this.configuration = configuration;
        this.premium = premium;
    }

    protected BaseTop(GuildRepository guildRepository, Configuration configuration) {
        this(guildRepository, configuration, true);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (premium && Premium.isNotEntitled(context, configuration.skus().features().advancedRankings().advancedRankings())) {
            Premium.replyPremium(context, configuration.skus().features().advancedRankings().advancedRankings());
            return;
        }

        var guild = guildRepository.guild(event.getGuild());
        var reputationMode = guild.settings().general().reputationMode();
        if (event.getOption("mode") != null) {
            var mode = event.getOption("mode").getAsString();
            reputationMode = switch (mode) {
                case "total" -> ReputationMode.TOTAL;
                case "7 days" -> ReputationMode.ROLLING_WEEK;
                case "30 days" -> ReputationMode.ROLLING_MONTH;
                case "week" -> ReputationMode.WEEK;
                case "month" -> ReputationMode.MONTH;
                default -> reputationMode;
            };
        }

        registerPage(buildRanking(event, guild, reputationMode, TOP_PAGE_SIZE), context);
    }

    protected abstract Ranking buildRanking(SlashCommandInteractionEvent event, RepGuild guild, ReputationMode reputationMode, int pageSize);

    public static MessageEditData buildRanking(List<RankingEntry> ranking, Ranking guildRanking, LocalizationContext context) {
        if (ranking.isEmpty()) {
            return BaseTop.buildEmptyRanking(guildRanking, context);
        }
        var maxRank = ranking.get(ranking.size() - 1).rank();
        var rankString = ranking.stream().map(rank -> rank.fancyString((int) maxRank))
                                .collect(Collectors.joining("\n"));

        return MessageEditData.fromEmbeds(BaseTop.createBaseBuilder(guildRanking, context)
                                                 .setDescription(rankString)
                                                 .build());
    }

    private static MessageEditData buildEmptyRanking(Ranking ranking, LocalizationContext context) {
        return MessageEditData.fromEmbeds(BaseTop.createBaseBuilder(ranking, context)
                                                 .setDescription("*" + context.localize("ranking.empty") + "*")
                                                 .build());
    }

    protected static LocalizedEmbedBuilder createBaseBuilder(Ranking ranking, LocalizationContext context) {
        return new LocalizedEmbedBuilder(context)
                .setTitle(ranking.title(), ranking.replacement())
                .setColor(Color.CYAN);
    }

    public void registerPage(Ranking ranking, EventContext context) {
        context.registerPage(new PageBag(ranking.pages()) {
            @Override
            public CompletableFuture<MessageEditData> buildPage() {
                return CompletableFuture.supplyAsync(() -> {
                    var entries = ranking.page(current());
                    var maxRank = entries.get(entries.size() - 1).rank();
                    var rankString = entries.stream().map(rank -> rank.fancyString((int) maxRank))
                                            .collect(Collectors.joining("\n"));

                    return MessageEditData.fromEmbeds(createBaseBuilder(ranking, context.guildLocalizer())
                            .setDescription(rankString)
                            .build());
                });
            }

            @Override
            public CompletableFuture<MessageEditData> buildEmptyPage() {
                return CompletableFuture.completedFuture(MessageEditData.fromEmbeds(createBaseBuilder(ranking, context.guildLocalizer())
                        .setDescription("*" + context.localize("ranking.empty") + "*")
                        .build()));
            }
        }, true);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var option = event.getFocusedOption();
        if ("mode".equalsIgnoreCase(option.getName())) {
            event.replyChoices(Completion.complete(option.getValue(), "total", "7 days", "30 days", "week", "month")).queue();
        }
    }
}
