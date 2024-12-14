/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler.system;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.bag.ListPageBag;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handles the metrics-related slash commands.
 */
public class Metrics implements SlashHandler {
    private final Configuration configuration;

    /**
     * Constructs a new Metrics handler with the specified configuration.
     *
     * @param configuration the configuration to use
     */
    public Metrics(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        List<String> links = new ArrayList<>();
        links.add(url("reputation/count/month/1/24"));
        links.add(url("reputation/total/month/0/24"));
        links.add(url("reputation/dow/month/1"));
        links.add(url("reputation/type/total/month/0/48"));
        links.add(url("reputation/type/count/month/0/48"));

        links.add(url("commands/count/week/1/52"));
        links.add(url("commands/usage/week/1"));
        links.add(url("commands/usage/month/1"));

        links.add(url("messages/count/week/1/52"));
        links.add(url("messages/total/week/0/52"));

        links.add(url("users/active/week/1/52"));
        links.add(url("users/active/month/1/24"));

        links.add(url("service/count/hour/0/120"));
        links.add(url("service/count/week/0/104"));

        context.registerPage(new ListPageBag<>(links) {
            @Override
            public CompletableFuture<MessageEditData> buildPage() {
                return CompletableFuture.completedFuture(MessageEditData.fromEmbeds(new EmbedBuilder()
                        .setImage(currentElement())
                        .setAuthor("Open in Browser", currentElement(), event.getJDA().getSelfUser().getAvatarUrl())
                        .build()));
            }
        });
    }

    /**
     * Constructs a URL for the specified endpoint.
     *
     * @param endpoint the endpoint to construct the URL for
     * @return the constructed URL
     */
    private String url(String endpoint) {
        return "%s/v1/metrics/%s".formatted(configuration.api().url(), endpoint);
    }
}
