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
}
