/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku;

import de.chojo.jdautil.interactions.base.SkuMeta;
import de.chojo.jdautil.interactions.premium.SKU;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SKUEntry implements SkuMeta {
    private List<SKU> skus = new ArrayList<>();

    public SKUEntry() {
    }

    public SKUEntry(List<SKU> skus) {
        this.skus = skus;
    }

    @Override
    public Collection<SKU> sku() {
        return skus;
    }
}
