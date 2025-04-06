/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import de.chojo.jdautil.interactions.premium.SKUConfiguration;
import de.chojo.repbot.config.elements.sku.SKUFeatures;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class SKU {
    de.chojo.jdautil.interactions.premium.SKUConfiguration interactions = new SKUConfiguration();
    SKUFeatures features = new SKUFeatures();

    public de.chojo.jdautil.interactions.premium.SKUConfiguration interactions() {
        return interactions;
    }

    public SKUFeatures features() {
        return features;
    }
}
