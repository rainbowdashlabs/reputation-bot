/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.sku.SKUEntry;

public class Autopost {
    private SKUEntry autopostChannel = new SKUEntry();

    public SKUEntry autopostChannel() {
        return autopostChannel;
    }
}
