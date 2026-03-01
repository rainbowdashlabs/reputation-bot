/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild.features;

import de.chojo.repbot.dao.access.guild.subscriptions.TokenPurchase;

import java.time.Instant;

public record ActiveFeaturePOJO(int featureId, Instant expires, boolean autoRenewal) {
    public static ActiveFeaturePOJO generate(TokenPurchase purchase) {
        return new ActiveFeaturePOJO(purchase.featureId(), purchase.expires(), purchase.autoRenewal());
    }
}
