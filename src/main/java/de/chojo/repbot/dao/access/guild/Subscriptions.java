/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild;

import de.chojo.jdautil.interactions.base.SkuMeta;
import de.chojo.jdautil.interactions.premium.SKU;
import de.chojo.repbot.dao.access.guild.subscriptions.SkuTarget;
import de.chojo.repbot.dao.access.guild.subscriptions.Subscription;
import de.chojo.repbot.dao.access.guild.subscriptions.SubscriptionError;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.util.SupporterFeature;
import de.chojo.sadu.queries.api.results.writing.insertion.InsertionResult;
import de.chojo.sadu.queries.call.adapter.StandardAdapter;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Subscriptions implements GuildHolder, SkuMeta {

    private final RepGuild repGuild;
    private List<Subscription> subscriptions;
    private Map<SupporterFeature, SubscriptionError> errorMessages = null;

    public Subscriptions(RepGuild repGuild) {
        this.repGuild = repGuild;
    }

    @Override
    public Guild guild() {
        return repGuild.guild();
    }

    @Override
    public long guildId() {
        return repGuild.guildId();
    }

    @Override
    public Collection<SKU> sku() {
        return subscriptions().stream().map(e -> (SKU) e).toList();
    }

    public int maxErrorCount() {
        return errorMessages.values().stream().mapToInt(SubscriptionError::count).max().orElse(0);
    }

    public void deleteSubscription(Subscription subscription) {
        if (subscription.isPersistent()) return;
        query("""
                DELETE FROM subscriptions WHERE id = ? AND sku = ?;
                """)
                .single(call().bind(subscription.id()).bind(subscription.skuId()))
                .delete();
        subscriptions().remove(subscription);
    }

    @NotNull
    public SubscriptionError getErrorMessage(SupporterFeature error) {
        return errorMessages().getOrDefault(error, error.first());
    }

    public void deleteError(SupporterFeature error) {
        query("""
                DELETE FROM subscription_error WHERE guild_id = ? AND type = ?
                """).single(call().bind(guildId()).bind(error))
                    .delete();
        errorMessages().remove(error);
    }

    public List<SubscriptionError> getExpiredErrors(int hours) {
        return errorMessages().values()
                              .stream()
                              .filter(e -> e.lastSend().plus(hours, ChronoUnit.HOURS).isAfter(Instant.now()))
                              .toList();
    }

    public void resendError(SupporterFeature error, boolean notified) {
        query("""
                INSERT
                INTO
                    subscription_error(guild_id, type, last_send, notified)
                VALUES
                    (?, ?, now(), ?)
                ON CONFLICT(guild_id, type)
                    DO UPDATE
                    SET
                        last_send = now(),
                        count     = excluded.count + 1,
                        notified  = excluded.notified
                RETURNING guild_id, type, last_send, count, date_inserted, notified
                """)
                .single(call().bind(guildId()).bind(error).bind(notified))
                .map(SubscriptionError::build)
                .first()
                .ifPresent(e -> errorMessages().put(e.type(), e));
    }

    public List<Subscription> subscriptions() {
        if (subscriptions == null) {
            subscriptions = query("""
                    SELECT
                        id,
                        sku,
                        type,
                        ends_at,
                        purchase_type,
                        persistent
                    FROM
                        subscriptions
                    WHERE id = ?
                      AND type = ?
                    """).single(call().bind(repGuild.guildId()).bind(SkuTarget.GUILD))
                        .map(Subscription.map())
                        .all();
        }
        return subscriptions;
    }

    public boolean addSubscription(Subscription subscription) {
        InsertionResult result = query("""
                INSERT
                INTO
                    subscriptions(id, sku, type, ends_at, purchase_type, persistent)
                VALUES
                    (?, ?, ?, ?, ?, ?)
                ON CONFLICT(id, sku) DO UPDATE SET
                                                   ends_at       = excluded.ends_at,
                                                   persistent    = excluded.persistent,
                                                   purchase_type = excluded.purchase_type""")
                .single(call().bind(subscription.id())
                              .bind(subscription.skuId())
                              .bind(subscription.skuTarget())
                              .bind(subscription.endsAt(), StandardAdapter.INSTANT_AS_TIMESTAMP)
                              .bind(subscription.purchaseType())
                              .bind(subscription.isPersistent()))
                .insert();
        if (result.changed()) {
            subscriptions().remove(subscription);
            subscriptions().add(subscription);
            return true;
        }
        return false;
    }

    public void invalidate() {
        subscriptions = null;
    }

    public void clear() {
        query("""
                DELETE FROM subscriptions WHERE id = ? AND type = ? AND NOT persistent;
                """)
                .single(call().bind(repGuild.guildId()).bind(SkuTarget.GUILD))
                .update();
        invalidate();
    }

    private synchronized Map<SupporterFeature, SubscriptionError> errorMessages() {
        if (errorMessages == null) {
            errorMessages = query("""
                    SELECT
                        guild_id,
                        type,
                        last_send,
                        count,
                        date_inserted,
                        notified
                    FROM
                        subscription_error
                    WHERE guild_id = ?
                    """).single(call().bind(guildId()))
                        .map(SubscriptionError::build)
                        .all()
                        .stream()
                        .collect(Collectors.toMap(SubscriptionError::type, Function.identity(),
                                (a, b) -> a,
                                () -> new EnumMap<>(SupporterFeature.class)));
        }
        return errorMessages;
    }
}
