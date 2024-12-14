/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.statistic;

import de.chojo.jdautil.localization.util.Replacement;

import java.util.List;

/**
 * Functional interface for providing a list of replacements.
 */
@FunctionalInterface
public interface ReplacementProvider {
    /**
     * Returns a list of replacements.
     *
     * @return a list of {@link Replacement} objects
     */
    List<Replacement> replacements();
}
