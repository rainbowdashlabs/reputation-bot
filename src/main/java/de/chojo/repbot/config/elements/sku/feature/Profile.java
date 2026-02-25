/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.sku.tokens.Feature;

/**
 * Allows having a custom bot profile.
 * Including a profile, profile picture and description.
 */
public class Profile extends Feature {

    public Profile() {
        super(9, 350);
    }
}
