/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.sku.tokens.Feature;

public class ReputationChannel extends Feature {
    private int defaultChannel = 2;

    public ReputationChannel() {
        super(10, 175);
    }

    public int defaultChannel() {
        return defaultChannel;
    }
}
