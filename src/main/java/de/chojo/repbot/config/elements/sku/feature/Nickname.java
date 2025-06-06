/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.SKU;
import de.chojo.repbot.config.elements.sku.SKUEntry;

public class Nickname {
    private SKUEntry allow = new SKUEntry();

    public SKUEntry allow() {
        return allow;
    }
}
