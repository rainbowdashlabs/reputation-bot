/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.error;

import de.chojo.repbot.web.pojo.premium.SkuInfo;

import java.util.List;

/**
 * Detailed information about a premium feature restriction.
 */
public class PremiumFeatureErrorDetails {
    private final String feature;
    private final List<SkuInfo> requiredSkus;
    private final Integer currentValue;
    private final Integer maxValue;

    public PremiumFeatureErrorDetails(
            String feature, List<SkuInfo> requiredSkus, Integer currentValue, Integer maxValue) {
        this.feature = feature;
        this.requiredSkus = requiredSkus;
        this.currentValue = currentValue;
        this.maxValue = maxValue;
    }

    public PremiumFeatureErrorDetails(String feature, List<SkuInfo> requiredSkus) {
        this(feature, requiredSkus, null, null);
    }

    public String feature() {
        return feature;
    }

    public List<SkuInfo> requiredSkus() {
        return requiredSkus;
    }

    public Integer currentValue() {
        return currentValue;
    }

    public Integer maxValue() {
        return maxValue;
    }
}
