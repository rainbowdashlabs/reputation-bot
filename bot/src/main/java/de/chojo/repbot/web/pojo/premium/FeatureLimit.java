/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.premium;

import java.util.List;

import static de.chojo.repbot.util.States.GRANT_ALL_SKU;

/**
 * Represents a premium feature with a limit.
 * Contains information about maximum allowed, unlock status, and which SKUs can unlock it.
 * The current usage count can be derived from the settings.
 */
public class FeatureLimit {
    private final int max;
    private final boolean unlocked;
    private final List<SkuInfo> requiredSkus;

    public FeatureLimit(int max, boolean unlocked, List<SkuInfo> requiredSkus) {
        this.max = max;
        this.unlocked = unlocked;
        this.requiredSkus = requiredSkus;
    }

    /**
     * Maximum allowed count
     */
    public int max() {
        return max;
    }

    /**
     * Whether the premium feature is unlocked
     */
    public boolean unlocked() {
        if (GRANT_ALL_SKU) return true;
        return unlocked;
    }

    /**
     * List of SKUs that can unlock this feature
     */
    public List<SkuInfo> requiredSkus() {
        return requiredSkus;
    }
}
