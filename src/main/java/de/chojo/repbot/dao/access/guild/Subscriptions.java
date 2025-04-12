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
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.queries.api.results.writing.insertion.InsertionResult;
import de.chojo.sadu.queries.call.adapter.StandardAdapter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Collection;
import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Subscriptions implements GuildHolder, SkuMeta {

    private final RepGuild repGuild;
    private List<Subscription> subscriptions;

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

    public List<Subscription> subscriptions() {
        if (subscriptions == null) {
            subscriptions = query("""
                    SELECT
                        id,
                        sku,
                        type,
                        ends_at
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

    public void addSubscription(Subscription subscription) {
        InsertionResult result = query("""
                INSERT
                INTO
                    subscriptions(id, sku, type, ends_at)
                VALUES
                    (?, ?, ?, ?)
                ON CONFLICT(id, sku) DO UPDATE SET ends_at = excluded.ends_at""")
                .single(call().bind(subscription.id()).bind(subscription.getSkuIdLong()).bind(subscription.skuTarget()).bind(subscription.endsAt(), StandardAdapter.INSTANT_AS_TIMESTAMP))
                .insert();
        if (result.changed()) {
            subscriptions().remove(subscription);
            subscriptions().add(subscription);
        }
    }

    public void invalidate() {
        subscriptions = null;
    }

    public void clear() {
        query("""
                DELETE FROM subscriptions WHERE id = ? AND type = ?
                """)
                .single(call().bind(repGuild.guildId()).bind(SkuTarget.GUILD))
                .update();
        invalidate();
    }
}
