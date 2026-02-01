/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.error;

import de.chojo.repbot.web.pojo.premium.SkuInfo;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception thrown when a user attempts to use a premium feature without proper entitlement.
 */
public class PremiumFeatureException extends ApiException {
    private final PremiumFeatureErrorDetails details;

    public PremiumFeatureException(String feature, List<SkuInfo> requiredSkus) {
        super(HttpStatus.FORBIDDEN, buildMessage(feature, requiredSkus));
        this.details = new PremiumFeatureErrorDetails(feature, requiredSkus);
    }

    public PremiumFeatureException(String feature, List<SkuInfo> requiredSkus, int currentValue, int maxValue) {
        super(HttpStatus.FORBIDDEN, buildMessage(feature, requiredSkus, currentValue, maxValue));
        this.details = new PremiumFeatureErrorDetails(feature, requiredSkus, currentValue, maxValue);
    }

    public PremiumFeatureErrorDetails details() {
        return details;
    }

    private static String buildMessage(String feature, List<SkuInfo> requiredSkus) {
        if (requiredSkus.isEmpty()) {
            return String.format("Premium feature '%s' is not available", feature);
        }
        String skuNames = requiredSkus.stream()
                .map(SkuInfo::name)
                .collect(Collectors.joining(", "));
        return String.format("Premium feature '%s' requires one of: %s", feature, skuNames);
    }

    private static String buildMessage(String feature, List<SkuInfo> requiredSkus, int currentValue, int maxValue) {
        if (requiredSkus.isEmpty()) {
            return String.format("Premium feature '%s' limit exceeded (current: %d, max: %d)", feature, currentValue, maxValue);
        }
        String skuNames = requiredSkus.stream()
                .map(SkuInfo::name)
                .collect(Collectors.joining(", "));
        return String.format("Premium feature '%s' limit exceeded (current: %d, max: %d). Requires one of: %s", feature, currentValue, maxValue, skuNames);
    }
}
