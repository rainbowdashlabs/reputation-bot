/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.user.sub.purchases;

import com.google.common.hash.Hashing;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.sku.Subscription;
import de.chojo.repbot.dao.access.user.sub.MailEntry;
import de.chojo.repbot.service.kofi.KofiShopItem;
import de.chojo.repbot.service.kofi.KofiTransaction;
import de.chojo.repbot.service.kofi.Type;
import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

/**
 * Represents a purchase made on kofi. This maybe be a subscription or a lifetime purchase.
 * The Mail hash might have a matching {@link MailEntry}
 */
public class KofiPurchase {
    private final long id;
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

    public boolean assignPurchaseToGuild(long guildId) {
        query("""
                UPDATE kofi_purchase SET guild_id = ? WHERE id = ?;
                """).single(call().bind(guildId).bind(id)).update();
        return true;
    }

    public boolean unassignPurchaseFromGuild() {
        query("""
                UPDATE kofi_purchase SET guild_id = 0 WHERE id = ?;
                """).single(call().bind(id)).update();
        return true;
    }

    @MappingProvider({"id", "mail_hash", "key", "sku_id", "type", "expires_at", "transaction_id", "guild_id"})
    public KofiPurchase(Row row) throws SQLException {
        this(
                row.getLong("id"),
                row.getString("mail_hash"),
                row.getString("transaction_id"),
                row.getString("key"),
                row.getEnum("type", Type.class),
                row.getLong("sku_id"),
                row.get("expires_at", INSTANT_TIMESTAMP),
                row.getLong("guild_id"));
    }

    public KofiPurchase(String mailHash, String transactionId, String key, Type type, long skuId, Instant expiresAt) {
        this(-1, mailHash, transactionId, key, type, skuId, expiresAt, 0);
    }

    public KofiPurchase(
            long id,
            String mailHash,
            String transactionId,
            String key,
            Type type,
            long skuId,
            Instant expiresAt,
            long guildId) {
        this.id = id;
        this.mailHash = mailHash;
        this.transactionId = transactionId;
        this.guildId = guildId;
        this.key = key;
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
                    Instant.now().plus(32, ChronoUnit.DAYS)));
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
                            null));
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
                UPDATE kofi_purchase SET expires_at = now() + '32 days'::INTERVAL WHERE id = ?
                """).single(call().bind(id)).update();
    }

    public boolean isValid() {
        return expiresAt.isAfter(Instant.now().minus(32, ChronoUnit.DAYS));
    }

    public void delete() {
        query("""
            DELETE FROM kofi_purchase WHERE id = ?;
            """)
                .single(call().bind(id))
                .delete();
    }
}
