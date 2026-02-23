/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.sku.tokens.Feature;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class ReputationLog extends Feature {
    private int defaultSize = 2;

    public ReputationLog() {
        super(11, 350);
    }

    public int defaultSize() {
        return defaultSize;
    }
}
