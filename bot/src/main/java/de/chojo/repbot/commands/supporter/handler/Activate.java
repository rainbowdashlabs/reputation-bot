/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.supporter.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.sku.Subscription;
import de.chojo.repbot.service.PremiumService;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Activate extends BaseSupporter implements SlashHandler {
    private final PremiumService premiumService;

    public Activate(PremiumService premiumService, Configuration configuration) {
        super(configuration);
        this.premiumService = premiumService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Long coupon;
        try {
            coupon = event.getOption("coupon", OptionMapping::getAsLong);
        } catch (NumberFormatException e) {
            event.reply(context.localize("command.supporter.activate.message.invalidcoupon"))
                    .setEphemeral(true)
                    .complete();
            return;
        }

        if (coupon == null) return;
        event.deferReply().setEphemeral(true).queue();
        var match = event
                .getJDA()
                .retrieveEntitlements()
                .user(event.getUser())
                .skuIds(configuration.skus().subscriptions().stream()
                        .mapToLong(Subscription::lifetimeSku)
                        .toArray())
                .complete()
                .stream()
                .filter(this::isLifetimeSku)
                .filter(this::isAvailable)
                .filter(e -> e.getIdLong() == coupon)
                .findFirst();

        if (match.isEmpty()) {
            event.getHook()
                    .editOriginal(context.localize("command.supporter.activate.message.invalidcoupon"))
                    .complete();
            return;
        }

        Subscription subscription = getSubscription(match.get());
        if (premiumService.activateLifetime(event.getGuild(), subscription)) {
            match.get().consume().complete();
            event.getHook()
                    .editOriginal(context.localize(
                            "command.supporter.activate.message.activated",
                            Replacement.create("TIER", subscription.name())))
                    .queue();
            return;
        }
        event.getHook()
                .editOriginal(context.localize("command.supporter.activate.message.failed"))
                .queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var choices = event
                .getJDA()
                .retrieveEntitlements()
                .user(event.getUser())
                .skuIds(configuration.skus().subscriptions().stream()
                        .mapToLong(Subscription::lifetimeSku)
                        .toArray())
                .complete()
                .stream()
                .filter(this::isLifetimeSku)
                .filter(this::isAvailable)
                .map(e -> {
                    Subscription subscription = getSubscription(e);
                    return new Command.Choice(subscription.name(), e.getIdLong());
                })
                .toList();
        event.replyChoices(choices).queue();
    }
}
