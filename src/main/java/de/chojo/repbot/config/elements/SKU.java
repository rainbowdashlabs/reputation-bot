/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import de.chojo.jdautil.interactions.premium.SKUConfiguration;
import de.chojo.repbot.config.elements.sku.SKUFeatures;
import de.chojo.repbot.config.elements.sku.Subscription;
import net.dv8tion.jda.api.entities.Entitlement;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class SKU {
    List<Subscription> subscriptions = List.of(new Subscription());
    SKUConfiguration interactions = new SKUConfiguration();
    SKUFeatures features = new SKUFeatures();

    int subscriptionErrorMessageHours = 36;
    int errorThresholdBlock = 10;

    public SKUConfiguration interactions() {
        return interactions;
    }

    public SKUFeatures features() {
        return features;
    }

    public int subscriptionErrorMessageHours() {
        return subscriptionErrorMessageHours;
    }

    public int errorThresholdBlock() {
        return errorThresholdBlock;
    }

    /**
     * The end of the grace period for a blocking supporter feature: one notice interval per allowed notice.
     *
     * @param reportedSince when the feature was first reported
     * @return the moment the guild stops receiving reputation
     */
    public Instant gracePeriodEnd(Instant reportedSince) {
        return reportedSince.plus((long) errorThresholdBlock * subscriptionErrorMessageHours, ChronoUnit.HOURS);
    }

    public List<Subscription> subscriptions() {
        return subscriptions;
    }

    public boolean isLifetime(Entitlement entitlement) {
        return subscriptions.stream().anyMatch(sub -> sub.lifetimeSku() == entitlement.getSkuIdLong());
    }

    public Optional<Subscription> getSubscriptionForLifetime(long lifetimeSkuId) {
        return subscriptions.stream()
                .filter(sub -> sub.lifetimeSku() == lifetimeSkuId)
                .findAny();
    }
}
