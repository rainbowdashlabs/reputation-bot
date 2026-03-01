/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.sku.tokens.Feature;

public class ReputationCategories extends Feature {
    private int defaultCategories = 2;

    public ReputationCategories() {
        super(10, 175);
    }

    public int defaultCategories() {
        return defaultCategories;
    }
}
