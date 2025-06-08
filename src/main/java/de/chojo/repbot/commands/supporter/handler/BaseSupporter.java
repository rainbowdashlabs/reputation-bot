/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.supporter.handler;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.sku.Subscription;
import net.dv8tion.jda.api.entities.Entitlement;

public class BaseSupporter {
    protected final Configuration configuration;

    public BaseSupporter(Configuration configuration) {
        this.configuration = configuration;
    }

    protected boolean isLifetimeSku(Entitlement entitlement) {
        return configuration.skus().subscriptions().stream().anyMatch(sub -> entitlement.getSkuIdLong() == sub.lifetimeSku());
    }

    protected Subscription getSubscription(Entitlement entitlement) {
        return configuration.skus().subscriptions().stream()
                            .filter(sub -> entitlement.getSkuIdLong() == sub.lifetimeSku())
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("No subscription found for entitlement " + entitlement.getSkuIdLong()));
    }

    protected boolean isAvailable(Entitlement entitlement) {
        return !entitlement.isConsumed() && !entitlement.isDeleted();
    }
}
