/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.sku.SKUEntry;

public class ReputationCategories {
    private SKUEntry moreCategories = new SKUEntry();
    private int defaultCategories = 2;

    public SKUEntry moreCategories() {
        return moreCategories;
    }

    public int defaultCategories() {
        return defaultCategories;
    }
}
