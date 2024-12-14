/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.top.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.pagination.bag.PageBag;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.pagination.GuildRanking;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Handler for the "show top" slash command.
 * This command displays the top rankings for the guild based on reputation.
 */
public class Show implements SlashHandler {
    private static final int TOP_PAGE_SIZE = 10;
    private final Guilds guilds;

    /**
     * Constructs a new Show handler.
     *
     * @param guilds the guilds provider
     */
    public Show(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guilds.guild(event.getGuild());
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

        var ranking = guild.reputation().ranking().byMode(reputationMode, TOP_PAGE_SIZE);
        registerPage(ranking, event, context);
    }

    /**
     * Registers a page for the ranking.
     *
     * @param guildRanking the guild ranking
     * @param event the slash command interaction event
     * @param context the event context
     */
    public static void registerPage(GuildRanking guildRanking, SlashCommandInteractionEvent event, EventContext context) {
        context.registerPage(new PageBag(guildRanking.pages()) {
            @Override
            public CompletableFuture<MessageEditData> buildPage() {
                return CompletableFuture.supplyAsync(() -> {
                    var ranking = guildRanking.page(current());

                    var maxRank = ranking.get(ranking.size() - 1).rank();
                    var rankString = ranking.stream().map(rank -> rank.fancyString((int) maxRank))
                                            .collect(Collectors.joining("\n"));

                    return MessageEditData.fromEmbeds(createBaseBuilder(guildRanking, context, event.getGuild())
                            .setDescription(rankString)
                            .build());
                });
            }

            @Override
            public CompletableFuture<MessageEditData> buildEmptyPage() {
                return CompletableFuture.completedFuture(MessageEditData.fromEmbeds(createBaseBuilder(guildRanking, context, event.getGuild())
                        .setDescription("*" + context.localize("command.top.message.empty") + "*")
                        .build()));
            }
        }, true);
    }

    /**
     * Creates a base embed builder for the ranking.
     *
     * @param guildRanking the guild ranking
     * @param context the event context
     * @param guild the guild
     * @return the localized embed builder
     */
    private static LocalizedEmbedBuilder createBaseBuilder(GuildRanking guildRanking, EventContext context, Guild guild) {
        return new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle(guildRanking.title(), Replacement.create("GUILD", guild.getName()))
                .setColor(Color.CYAN);
    }

    /**
     * Handles the auto-complete interaction event.
     *
     * @param event the auto-complete interaction event
     * @param context the event context
     */
    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var option = event.getFocusedOption();
        if ("mode".equalsIgnoreCase(option.getName())) {
            event.replyChoices(Completion.complete(option.getValue(), "total", "7 days", "30 days", "week", "month")).queue();
        }
    }
}
