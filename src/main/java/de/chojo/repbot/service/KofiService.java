/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.interactions.premium.SKU;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.core.Threading;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.Subscriptions;
import de.chojo.repbot.dao.access.guild.subscriptions.Subscription;
import de.chojo.repbot.dao.access.user.RepUser;
import de.chojo.repbot.dao.access.user.sub.MailEntry;
import de.chojo.repbot.dao.access.user.sub.MailSource;
import de.chojo.repbot.dao.access.user.sub.purchases.KofiPurchase;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.service.kofi.KofiTransaction;
import de.chojo.repbot.service.kofi.SubscriptionResult;
import de.chojo.repbot.service.kofi.Type;
import de.chojo.repbot.service.mailservice.FailureReason;
import de.chojo.repbot.service.mailservice.Mail;
import de.chojo.repbot.util.LogNotify;
import de.chojo.repbot.util.Result;
import io.javalin.http.UnauthorizedResponse;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static de.chojo.repbot.dao.access.guild.subscriptions.SkuTarget.GUILD;
import static de.chojo.repbot.dao.access.guild.subscriptions.SubscriptionSource.KOFI;
import static net.dv8tion.jda.api.entities.Entitlement.EntitlementType.APPLICATION_SUBSCRIPTION;
import static org.slf4j.LoggerFactory.getLogger;

public class KofiService {
    private static final Logger log = getLogger(KofiService.class);
    private final UserRepository userRepository;
    private final GuildRepository guildRepository;
    private final ShardManager shardManager;
    private final Configuration configuration;
    private final MailService mailService;

    public KofiService(
            UserRepository userRepository,
            GuildRepository guildRepository,
            ShardManager shardManager,
            Configuration configuration,
            MailService mailService,
            Threading threading) {
        this.userRepository = userRepository;
        this.guildRepository = guildRepository;
        this.shardManager = shardManager;
        this.configuration = configuration;
        this.mailService = mailService;
        threading.repBotWorker().scheduleAtFixedRate(this::removeExpiredSubs, 40, 60, TimeUnit.MINUTES);
    }

    public void handle(KofiTransaction data) {
        if (!configuration.kofi().token().equals(data.verificationToken())) {
            throw new UnauthorizedResponse();
        }

        // Check whether we know the user already that purchased something.
        Optional<RepUser> repUser = userRepository.byMailHash(mailService.mailHash(data.email()));
        if (repUser.isEmpty()) {
            Optional<User> user = resolveUser(data);
            if (user.isPresent()) {
                // Register email adress for that user if there is a registered discord account.
                Result<MailEntry, FailureReason> mailEntryFailureReasonResult =
                        mailService.registerVerifiedMail(user.get().getIdLong(), data.email(), MailSource.KOFI);
                if (mailEntryFailureReasonResult.isFailure()) {
                    log.error(
                            LogNotify.NOTIFY_ADMIN,
                            "Could not register verified mail {} for user {}: {}",
                            data.email(),
                            user.get().getIdLong(),
                            mailEntryFailureReasonResult.failureReason());
                }
            } else {
                mailService.sendMail(
                        Mail.kofiUserNotFound(data.email(), configuration.api().url()));
            }
        }

        if (data.type() == Type.SHOP_ORDER) {
            List<KofiPurchase> kofiPurchases = KofiPurchase.create(data, configuration);
            for (KofiPurchase kofiPurchase : kofiPurchases) {
                userRepository.registerPurchase(kofiPurchase);
            }
        }
    }

    public SubscriptionResult enableSubscription(KofiPurchase purchase, Guild guild) {
        RepGuild repGuild = guildRepository.guild(guild);
        Subscriptions subs = repGuild.subscriptions();

        if (purchase.type() == Type.SUBSCRIPTION) {
            if (!purchase.isValid()) return SubscriptionResult.SUBSCRIPTION_EXPIRED;
            if (subs.isEntitled(new SKU(purchase.skuId()))) return SubscriptionResult.ALREADY_SUBSCRIBED;
            if (purchase.guildId() != 0) disableSubscription(purchase);
            subs.addSubscription(new Subscription(
                    purchase.skuId(), guild.getIdLong(), KOFI, GUILD, APPLICATION_SUBSCRIPTION, null, true));
            return SubscriptionResult.SUCCESS;
        } else if (purchase.type() == Type.SHOP_ORDER) {
            var sub = configuration.skus().getSubscriptionForLifetime(purchase.skuId());
            if (sub.isEmpty()) {
                log.error(LogNotify.NOTIFY_ADMIN, "Could not find subscription for lifetime sku {}", purchase.skuId());
                return SubscriptionResult.UNKOWN;
            }
            if (subs.isEntitled(new SKU(sub.get().subscriptionSku()))) return SubscriptionResult.ALREADY_SUBSCRIBED;
            if (purchase.guildId() != 0) disableSubscription(purchase);
            subs.addSubscription(new Subscription(
                    sub.get().subscriptionSku(), guild.getIdLong(), KOFI, GUILD, APPLICATION_SUBSCRIPTION, null, true));
        } else {
            // This should never happen
            log.error(LogNotify.NOTIFY_ADMIN, "Unknown purchase type {}", purchase.type());
            return SubscriptionResult.UNKOWN;
        }

        if (purchase.assignPurchaseToGuild(guild.getIdLong())) {
            return SubscriptionResult.SUCCESS;
        }
        return SubscriptionResult.UNKOWN;
    }

    public boolean disableSubscription(KofiPurchase purchase) {
        if (purchase.guildId() == 0) return false;
        RepGuild repGuild = guildRepository.byId(purchase.guildId());
        Subscriptions subs = repGuild.subscriptions();
        if (purchase.type() == Type.SUBSCRIPTION) {
            subs.deleteSubscription(new Subscription(
                    purchase.skuId(), purchase.guildId(), KOFI, GUILD, APPLICATION_SUBSCRIPTION, null, true));
        }
        if (purchase.type() == Type.SHOP_ORDER) {
            var sub = configuration.skus().getSubscriptionForLifetime(purchase.skuId());
            if (sub.isEmpty()) {
                log.error(LogNotify.NOTIFY_ADMIN, "Could not find subscription for lifetime sku {}", purchase.skuId());
                return false;
            }
            subs.deleteSubscription(new Subscription(
                    sub.get().subscriptionSku(),
                    purchase.guildId(),
                    KOFI,
                    GUILD,
                    APPLICATION_SUBSCRIPTION,
                    null,
                    true));
        }

        return purchase.unassignPurchaseFromGuild();
    }

    private Optional<User> resolveUser(KofiTransaction data) {
        if (data.discordUserId() == null) return Optional.empty();
        try {
            return Optional.of(
                    shardManager.retrieveUserById(data.discordUserId()).complete());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void removeExpiredSubs() {
        for (KofiPurchase kofiPurchase : userRepository.getExpiredKofiPurchased()) {
            disableSubscription(kofiPurchase);
            kofiPurchase.delete();
        }
    }
}
