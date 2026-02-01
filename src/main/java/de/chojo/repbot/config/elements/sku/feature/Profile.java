/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.sku.SKUEntry;

/**
 * Allows having a custom bot profile.
 * Including a nickname, profile picture and description.
 */
public class Profile {
    private SKUEntry allow = new SKUEntry();

    public SKUEntry allow() {
        return allow;
    }
}
