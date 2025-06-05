/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.ranking.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.pagination.bag.PageBag;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.pagination.GuildRanking;
import de.chojo.repbot.dao.snapshots.RankingEntry;
import de.chojo.repbot.dao.snapshots.RepProfile;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class BaseTop implements SlashHandler {
    protected static final int TOP_PAGE_SIZE = 10;

    public static MessageEditData buildRanking(List<RankingEntry> ranking, GuildRanking guildRanking, Guild guild, LocalizationContext context) {
        if (ranking.isEmpty()) {
            return BaseTop.buildEmptyRanking(guildRanking, guild, context);
        }
        var maxRank = ranking.get(ranking.size() - 1).rank();
        var rankString = ranking.stream().map(rank -> rank.fancyString((int) maxRank))
                                .collect(Collectors.joining("\n"));

        return MessageEditData.fromEmbeds(BaseTop.createBaseBuilder(guildRanking, context, guild)
                                                 .setDescription(rankString)
                                                 .build());
    }

    private static MessageEditData buildEmptyRanking(GuildRanking guildRanking, Guild guild, LocalizationContext context) {
        return MessageEditData.fromEmbeds(BaseTop.createBaseBuilder(guildRanking, context, guild)
                                                 .setDescription("*" + context.localize("command.top.message.empty") + "*")
                                                 .build());
    }

    protected static LocalizedEmbedBuilder createBaseBuilder(GuildRanking guildRanking, LocalizationContext context, Guild guild) {
        return new LocalizedEmbedBuilder(context)
                .setTitle(guildRanking.title(), Replacement.create("GUILD", guild.getName()))
                .setColor(Color.CYAN);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var option = event.getFocusedOption();
        if ("mode".equalsIgnoreCase(option.getName())) {
            event.replyChoices(Completion.complete(option.getValue(), "total", "7 days", "30 days", "week", "month")).queue();
        }
    }

    public void registerPage(GuildRanking guildRanking, SlashCommandInteractionEvent event, EventContext context) {
        context.registerPage(new PageBag(guildRanking.pages()) {
            @Override
            public CompletableFuture<MessageEditData> buildPage() {
                return CompletableFuture.supplyAsync(() -> {
                    var ranking = guildRanking.page(current());

                    var maxRank = ranking.get(ranking.size() - 1).rank();
                    var rankString = ranking.stream().map(rank -> rank.fancyString((int) maxRank))
                                            .collect(Collectors.joining("\n"));

                    return MessageEditData.fromEmbeds(createBaseBuilder(guildRanking, context.guildLocalizer(), event.getGuild())
                            .setDescription(rankString)
                            .build());
                });
            }

            @Override
            public CompletableFuture<MessageEditData> buildEmptyPage() {
                return CompletableFuture.completedFuture(MessageEditData.fromEmbeds(createBaseBuilder(guildRanking, context.guildLocalizer(), event.getGuild())
                        .setDescription("*" + context.localize("command.top.message.empty") + "*")
                        .build()));
            }
        }, true);
    }
}
