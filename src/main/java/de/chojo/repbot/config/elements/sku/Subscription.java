/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku;

import de.chojo.jdautil.interactions.base.SkuMeta;
import de.chojo.jdautil.interactions.premium.SKU;

import java.util.List;

public class Subscription {
    String name = "";
    long subscriptionSku = 0;
    long lifetimeSku = 0;

    public String name() {
        return name;
    }

    public long subscriptionSku() {
        return subscriptionSku;
    }

    public long lifetimeSku() {
        return lifetimeSku;
    }

    public SkuMeta lifetimeSkuMeta() {
        return () -> List.of(new SKU(lifetimeSku));
    }

    public SkuMeta subscriptionSkuMeta() {
        return () -> List.of(new SKU(subscriptionSku));
    }
}
