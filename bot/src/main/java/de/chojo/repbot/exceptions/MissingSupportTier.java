/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.exceptions;

import de.chojo.jdautil.interactions.base.SkuMeta;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.util.SupporterFeature;

import java.util.Arrays;
import java.util.List;

public class MissingSupportTier extends RuntimeException {
    private SupporterFeature type;
    private SkuMeta requiredTier;
    private Replacement[] replacements;

    public MissingSupportTier(SupporterFeature type, SkuMeta requiredTier, Replacement... replacements) {
        super("", null, true, false);
        this.type = type;
        this.requiredTier = requiredTier;
        this.replacements = replacements;
    }

    public SupporterFeature type() {
        return type;
    }

    public SkuMeta requiredTier() {
        return requiredTier;
    }

    public List<Replacement> replacements() {
        return Arrays.stream(replacements).toList();
    }
}
