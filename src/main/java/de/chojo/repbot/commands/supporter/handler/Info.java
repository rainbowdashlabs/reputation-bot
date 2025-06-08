/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.supporter.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.subscriptions.Subscription;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.PremiumService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;
import java.util.stream.Collectors;

public class Info extends BaseSupporter implements SlashHandler {
    private final GuildRepository guilds;
    private final PremiumService premiumService;

    public Info(PremiumService premiumService, Configuration configuration, GuildRepository guilds) {
        super(configuration);
        this.premiumService = premiumService;
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.deferReply().setEphemeral(true).queue();
        var subs = guilds.guild(event.getGuild()).subscriptions().subscriptions();

        String tiers = configuration.skus().subscriptions().stream()
                                    .map(sub -> {
                                        Optional<Subscription> match = subs.stream().filter(s -> s.skuId() == sub.subscriptionSku()).findFirst();
                                        if (match.isEmpty())
                                            return "%s: $%s$".formatted(sub.name(), "command.supporter.info.message.notactive");
                                        return "%s: $%s$".formatted(sub.name(), match.get().isPersistent() ? "words.active" : "words.inactive");
                                    }).collect(Collectors.joining("\n"));

        String coupons = event.getJDA().retrieveEntitlements()
                              .user(event.getUser())
                              .skuIds(configuration.skus().subscriptions().stream().mapToLong(de.chojo.repbot.config.elements.sku.Subscription::lifetimeSku).toArray())
                              .complete()
                              .stream()
                              .filter(this::isLifetimeSku)
                              .filter(this::isAvailable)
                              .map(e -> {
                                  de.chojo.repbot.config.elements.sku.Subscription subscription = getSubscription(e);
                                  return subscription.name();
                              })
                              .collect(Collectors.joining("\n"));

        var content = "**$%s$**\n%s".formatted("command.supporter.info.message.tiers", tiers);

        if (!coupons.isBlank()) {
            content = "%s\n\n**$%s$**\n%s".formatted(content, "command.supporter.info.message.coupons", coupons);
        }

        event.getHook().editOriginal(context.localize(content)).queue();
    }
}
