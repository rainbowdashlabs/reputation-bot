/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.subscriptions;

import de.chojo.jdautil.interactions.premium.SKU;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.converter.StandardValueConverter;
import net.dv8tion.jda.api.entities.Entitlement;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class Subscription extends SKU {
    private final long id;
    private final SkuTarget skuTarget;
    private final Instant endsAt;

    public Subscription(long skuId, long id, SkuTarget skuTarget, Instant endsAt) {
        super(skuId);
        this.id = id;
        this.skuTarget = skuTarget;
        this.endsAt = endsAt;
    }

    public long id() {
        return id;
    }

    public SkuTarget skuTarget() {
        return skuTarget;
    }

    public Instant endsAt() {
        return endsAt;
    }

    public static RowMapping<Subscription> map() {
        return row -> new Subscription(
                row.getLong("sku"),
                row.getLong("id"),
                row.getEnum("type", SkuTarget.class),
                row.get("ends_at", INSTANT_TIMESTAMP));
    }

    public static Subscription fromEntitlement(Entitlement entitlement) {
        SkuTarget targetType = SkuTarget.fromEntitlement(entitlement);
        long target = entitlement.getGuildId() == null ? entitlement.getUserIdLong() : entitlement.getGuildIdLong();

        return new Subscription(entitlement.getSkuIdLong(),
                target,
                targetType,
                Optional.ofNullable(entitlement.getTimeEnding()).map(OffsetDateTime::toInstant).orElse(null));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Subscription that = (Subscription) o;
        return id == that.id && skuTarget == that.skuTarget;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(id);
        result = 31 * result + skuTarget.hashCode();
        return result;
    }
}
