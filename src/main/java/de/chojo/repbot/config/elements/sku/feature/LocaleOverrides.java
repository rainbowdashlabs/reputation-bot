/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.sku.SKUEntry;

public class LocaleOverrides {
    private SKUEntry reputationNameOverride = new SKUEntry();

    public SKUEntry reputationNameOverride() {
        return reputationNameOverride;
    }
}
