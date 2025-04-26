/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.sku.SKUEntry;

public class AnalyzerLog {
    private SKUEntry longerLogTime = new SKUEntry();
    private int extendedLogHours = 96;

    public SKUEntry longerLogTime() {
        return longerLogTime;
    }

    public int extendedLogHours() {
        return extendedLogHours;
    }
}
