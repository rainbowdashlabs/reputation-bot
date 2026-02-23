/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.jdautil.localization.util.LocaleProvider;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.sku.tokens.Feature;
import de.chojo.repbot.core.Localization;
import de.chojo.repbot.core.Threading;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.subscriptions.SkuTarget;
import de.chojo.repbot.dao.access.guild.subscriptions.Subscription;
import de.chojo.repbot.dao.access.guild.subscriptions.TokenPurchase;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.provider.TokenPurchaseRepository;
import de.chojo.repbot.dao.provider.VoteRepository;
import de.chojo.repbot.service.tokenpurchaseservice.PurchaseResult;
import de.chojo.repbot.util.EntityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static de.chojo.repbot.service.tokenpurchaseservice.FailureReason.GUILD_HAS_SUBSCRIPTION;
import static de.chojo.repbot.service.tokenpurchaseservice.FailureReason.GUILD_NOT_FOUND;
import static de.chojo.repbot.service.tokenpurchaseservice.FailureReason.INSUFFICIENT_GUILD_TOKENS;
import static de.chojo.repbot.service.tokenpurchaseservice.FailureReason.INSUFFICIENT_USER_TOKENS;
import static de.chojo.repbot.service.tokenpurchaseservice.FailureReason.UNKNOWN;
import static de.chojo.repbot.service.tokenpurchaseservice.FailureReason.UNKNOWN_FEATURE;
import static net.dv8tion.jda.api.entities.Entitlement.EntitlementType.APPLICATION_SUBSCRIPTION;

public class TokenPurchaseService {
    private final Configuration configuration;
    private final VoteRepository voteRepository;
    private final GuildRepository guildRepository;
    private final ShardManager shardManager;
    private final TokenPurchaseRepository tokenPurchaseRepository;
    private final Localization localization;

    public TokenPurchaseService(
            Configuration configuration,
            VoteRepository voteRepository,
            GuildRepository guildRepository,
            ShardManager shardManager,
            TokenPurchaseRepository tokenPurchaseRepository,
            Localization localization) {
        this.configuration = configuration;
        this.voteRepository = voteRepository;
        this.guildRepository = guildRepository;
        this.shardManager = shardManager;
        this.tokenPurchaseRepository = tokenPurchaseRepository;
        this.localization = localization;
    }

    public static TokenPurchaseService create(
            Configuration configuration,
            VoteRepository voteRepository,
            GuildRepository guildRepository,
            ShardManager shardManager,
            TokenPurchaseRepository tokenPurchaseRepository,
            Threading threading,
            Localization localizer) {
        TokenPurchaseService purchaseService = new TokenPurchaseService(
                configuration, voteRepository, guildRepository, shardManager, tokenPurchaseRepository, localizer);
        threading.repBotWorker().scheduleAtFixedRate(purchaseService::renewPurchases, 10, 60, TimeUnit.MINUTES);
        return purchaseService;
    }

    public PurchaseResult purchaseFeature(int id, long guildId, long entityId, EntityType entityType) {
        Optional<Feature> opt = configuration.skus().features().byId(id);
        if (opt.isEmpty()) return PurchaseResult.failed(UNKNOWN_FEATURE);
        Feature feature = opt.get();
        Guild guild = shardManager.getGuildById(guildId);
        if (guild == null) return PurchaseResult.failed(GUILD_NOT_FOUND);
        RepGuild repGuild = guildRepository.guild(guild);
        // Check whether the guild is already entitled by a purchased subscription.
        boolean entitled = repGuild.subscriptions().isEntitled(feature.skuEntry());
        if (entitled) return PurchaseResult.failed(GUILD_HAS_SUBSCRIPTION);
        // Check whether the paying entity has enough tokens.
        boolean withdrawn = voteRepository.withdrawTokens(entityId, entityType, feature.tokens());
        if (!withdrawn)
            return PurchaseResult.failed(
                    entityType == EntityType.GUILD ? INSUFFICIENT_GUILD_TOKENS : INSUFFICIENT_USER_TOKENS);
        repGuild.subscriptions().purchaseTokenFeature(feature);
        // Subscriptions by tokens are not managed by the premium service, therefore, they do not expire and are
        // persistent
        repGuild.subscriptions()
                .addSubscription(
                        new Subscription(feature.id(), guildId, SkuTarget.GUILD, APPLICATION_SUBSCRIPTION, null, true));
        return PurchaseResult.SUCCESS;
    }

    public PurchaseResult subscribeFeature(int id, long guildId) {
        Optional<Feature> opt = configuration.skus().features().byId(id);
        if (opt.isEmpty()) return PurchaseResult.failed(UNKNOWN_FEATURE);
        Feature feature = opt.get();
        Guild guild = shardManager.getGuildById(guildId);
        if (guild == null) return PurchaseResult.failed(GUILD_NOT_FOUND);
        RepGuild repGuild = guildRepository.guild(guild);
        Optional<TokenPurchase> optPurchase = repGuild.subscriptions().getTokenPurchase(feature.id());
        // If the server is not yet subscribed to that feature we try to purchase it first.
        if (optPurchase.isEmpty()) {
            PurchaseResult purchaseResult = purchaseFeature(feature.id(), guildId, guildId, EntityType.GUILD);
            if (!purchaseResult.success()) return purchaseResult;
        }
        optPurchase = repGuild.subscriptions().getTokenPurchase(feature.id());
        // This should not happen if the purchase was successful.
        if (optPurchase.isEmpty()) {
            return PurchaseResult.failed(UNKNOWN);
        }

        optPurchase.get().setAutoRenewal(true);
        return PurchaseResult.SUCCESS;
    }

    public PurchaseResult unsubscribeFeature(int id, long guildId) {
        Optional<Feature> opt = configuration.skus().features().byId(id);
        if (opt.isEmpty()) return PurchaseResult.failed(UNKNOWN_FEATURE);
        Feature feature = opt.get();
        Guild guild = shardManager.getGuildById(guildId);
        if (guild == null) return PurchaseResult.failed(GUILD_NOT_FOUND);
        RepGuild repGuild = guildRepository.guild(guild);
        Optional<TokenPurchase> optPurchase = repGuild.subscriptions().getTokenPurchase(feature.id());
        // If the server is not yet subscribed to that feature we try to purchase it first.
        if (optPurchase.isEmpty()) {
            // The purchase doesnt exist, so there is no need to unsubscribe
            return PurchaseResult.SUCCESS;
        }
        optPurchase.get().setAutoRenewal(false);
        return PurchaseResult.SUCCESS;
    }

    private void renewPurchases() {
        List<TokenPurchase> tokenPurchases = tokenPurchaseRepository.expiredPurchases();
        for (TokenPurchase purchase : tokenPurchases) {
            Guild guild = shardManager.getGuildById(purchase.guildId());
            // we leave this to the auto cleanup
            if (guild == null) continue;

            RepGuild repGuild = guildRepository.guild(guild);
            long id = repGuild.settings().general().systemChannel();
            TextChannel systemChannel = guild.getTextChannelById(id);
            Optional<Feature> feature = configuration.skus().features().byId(purchase.featureId());
            if (purchase.autoRenewal()) {
                // This happens if a guild subscribes to the supporter tiers after enabling a token feature.
                // In that case we simply cancel the subscription once it would be renewed.
                if (repGuild.subscriptions().isEntitled(feature.get().skuEntry())) {
                    repGuild.subscriptions()
                            .deleteSubscription(new Subscription(
                                    purchase.featureId(),
                                    purchase.guildId(),
                                    SkuTarget.GUILD,
                                    APPLICATION_SUBSCRIPTION,
                                    null,
                                    true));
                    continue;
                }
                PurchaseResult purchaseResult =
                        purchaseFeature(purchase.featureId(), purchase.guildId(), purchase.guildId(), EntityType.GUILD);
                if (purchaseResult.success()) continue;
                if (systemChannel != null) {
                    LocalizationContext context = localization.localizer().context(LocaleProvider.guild(guild));
                    String message = context.localize(
                            "$tokenpurchase.subscription.expired$ $%s$"
                                    .formatted(purchaseResult.reason().localeKey()),
                            Replacement.create(
                                    "FEATURE",
                                    feature.map(Feature::localeKey)
                                            .map(context::localize)
                                            .orElse("Unknown Feature")));
                    systemChannel
                            .sendMessage(message)
                            .queue(RestAction.getDefaultSuccess(), RestAction.getDefaultFailure());
                }
            }
            repGuild.subscriptions()
                    .deleteSubscription(new Subscription(
                            purchase.featureId(),
                            purchase.guildId(),
                            SkuTarget.GUILD,
                            APPLICATION_SUBSCRIPTION,
                            null,
                            true));
            if (!purchase.autoRenewal()) {
                if (systemChannel != null) {
                    LocalizationContext context = localization.localizer().context(LocaleProvider.guild(guild));
                    systemChannel
                            .sendMessage(context.localize(
                                    "tokenpurchase.purchase.expired",
                                    Replacement.create(
                                            "FEATURE",
                                            feature.map(Feature::localeKey)
                                                    .map(context::localize)
                                                    .orElse("Unknown Feature"))))
                            .queue(RestAction.getDefaultSuccess(), RestAction.getDefaultFailure());
                }
            }
        }
    }
}
