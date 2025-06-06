/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.sku.SKUEntry;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class ReputationLog {

    private SKUEntry extendedPages = new SKUEntry();
    private int defaultSize = 2;

    public SKUEntry extendedPages() {
        return extendedPages;
    }

    public int defaultSize() {
        return defaultSize;
    }
}
