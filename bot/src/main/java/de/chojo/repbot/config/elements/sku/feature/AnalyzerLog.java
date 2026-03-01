/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.sku.tokens.Feature;

public class AnalyzerLog extends Feature {
    private int extendedLogHours = 96;

    public AnalyzerLog() {
        super(2, 350);
    }

    public int extendedLogHours() {
        return extendedLogHours;
    }
}
