/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.statistic;

import de.chojo.jdautil.localization.util.Replacement;

import java.util.List;

@FunctionalInterface
public interface ReplacementProvider {
    List<Replacement> replacements();
}
