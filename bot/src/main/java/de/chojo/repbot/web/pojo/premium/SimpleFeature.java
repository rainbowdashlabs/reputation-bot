/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.premium;

import java.util.List;

/**
 * Represents a simple boolean premium feature.
 * Contains information about unlock status and which SKUs can unlock it.
 */
public class SimpleFeature {
    private final boolean unlocked;
    private final List<SkuInfo> requiredSkus;

    public SimpleFeature(boolean unlocked, List<SkuInfo> requiredSkus) {
        this.unlocked = unlocked;
        this.requiredSkus = requiredSkus;
    }

    /**
     * Whether the feature is unlocked
     */
    public boolean unlocked() {
        return unlocked;
    }

    /**
     * List of SKUs that can unlock this feature
     */
    public List<SkuInfo> requiredSkus() {
        return requiredSkus;
    }
}
