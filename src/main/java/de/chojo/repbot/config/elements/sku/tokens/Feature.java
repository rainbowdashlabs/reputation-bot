/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.tokens;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.chojo.jdautil.interactions.premium.SKU;
import de.chojo.repbot.config.elements.sku.SKUEntry;

import java.util.ArrayList;
import java.util.List;

public abstract class Feature {
    /**
     * General id of the feature.
     * This is used for token purchases
     */
    private int id;
    /**
     * Amount of tokens this feature costs per month
     */
    private int tokens;

    private String localeKey;
    private SKUEntry skuEntry = new SKUEntry();

    @JsonIgnore
    private transient SKUEntry fullSkuEntry;

    public Feature(int id, int tokens, String localeKey, SKUEntry skuEntry) {
        this.id = id;
        this.tokens = tokens;
        this.localeKey = localeKey;
        this.skuEntry = skuEntry;
    }

    public Feature(int id, int tokens) {
        this.id = id;
        this.tokens = tokens;
        this.localeKey = "sku.%s".formatted(getClass().getSimpleName().toLowerCase());
    }

    public String localeKey() {
        return localeKey;
    }

    public int id() {
        return id;
    }

    public int tokens() {
        return tokens;
    }

    /**
     * The defined sku entries for this feature excluding the token sku
     *
     * @return sku entry
     */
    public SKUEntry skuEntry() {
        return skuEntry;
    }

    /**
     * The full sku entry including the token sku
     *
     * @return sku entry
     */
    public SKUEntry fullSkuEntry() {
        if (fullSkuEntry == null) {
            List<SKU> sku = new ArrayList<>(skuEntry().sku());
            sku.add(new SKU(id));
            fullSkuEntry = new SKUEntry(sku);
        }
        return fullSkuEntry;
    }
}
