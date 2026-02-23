/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.user.sub.purchases;

import com.google.common.hash.Hashing;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.sku.Subscription;
import de.chojo.repbot.service.kofi.KofiShopItem;
import de.chojo.repbot.service.kofi.KofiTransaction;
import de.chojo.repbot.service.kofi.Type;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class KofiPurchase {
    long id;
    /**
     * A hash of the user mail.
     */
    private final String mailHash;

    private final String transactionId;
    /**
     * Key of the product
     * For purchases its the shop shortcode.
     * For subscriptions its the tier name.
     */
    private final String key;
    /**
     * The type of purchase.
     */
    private final Type type;
    /**
     * The sku id associated with this purchase.
     * For purchases its the lifetime sku.
     * For subscriptions its the subscription sku.
     */
    private final long skuId;
    /**
     * The date where this purchase expires.
     * Only applicable if {@link #type} is {@link Type#SUBSCRIPTION}.
     */
    private final Instant expiresAt;
    /**
     * The guild on which this purchase is active.
     */
    private final long guildId;

    public long id() {
        return id;
    }

    public String mailHash() {
        return mailHash;
    }

    public String transactionId() {
        return transactionId;
    }

    public String key() {
        return key;
    }

    public Type type() {
        return type;
    }

    public long skuId() {
        return skuId;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public long guildId() {
        return guildId;
    }

    public KofiPurchase(
            String mailHash,
            String transactionId,
            String platformKey,
            Type type,
            long skuId,
            Instant expiresAt,
            long guildId) {
        this.mailHash = mailHash;
        this.transactionId = transactionId;
        this.guildId = guildId;
        this.key = platformKey;
        this.type = type;
        this.skuId = skuId;
        this.expiresAt = expiresAt;
    }

    public static List<KofiPurchase> create(KofiTransaction transaction, Configuration configuration) {
        List<KofiPurchase> purchases = new ArrayList<>();
        String mailHash = Hashing.sha256()
                .hashString(transaction.email(), StandardCharsets.UTF_8)
                .toString();
        Type type = transaction.type();
        if (type == Type.SUBSCRIPTION) {
            Subscription subscription = configuration.skus().subscriptions().stream()
                    .filter(s -> s.kofiSubscriptionId().equals(transaction.tierName()))
                    .findFirst()
                    .orElseThrow();
            purchases.add(new KofiPurchase(
                    mailHash,
                    transaction.kofiTransactionId(),
                    transaction.tierName(),
                    type,
                    subscription.subscriptionSku(),
                    Instant.now().plus(32, ChronoUnit.DAYS),
                    0));
        } else if (type == Type.SHOP_ORDER) {
            int id = 0;
            for (KofiShopItem shopItem : transaction.shopItems()) {
                Subscription subscription = configuration.skus().subscriptions().stream()
                        .filter(s -> s.kofiLifetimeId().equals(shopItem.directLinkCode()))
                        .findFirst()
                        .orElseThrow();
                for (int i = 0; i < shopItem.quantity(); i++) {
                    purchases.add(new KofiPurchase(
                            mailHash,
                            transaction.kofiTransactionId() + "-" + (id++),
                            shopItem.directLinkCode(),
                            type,
                            subscription.lifetimeSku(),
                            null,
                            0));
                }
            }
        }
        return purchases;
    }

    /**
     * Renew the subscription.
     */
    public void renew() {
        query("""
            UPDATE kofi_purchase SET expires_at = NOW() + '32 days'::INTERVAL WHERE id = ?
            """).single(call().bind(id)).update();
    }
}
