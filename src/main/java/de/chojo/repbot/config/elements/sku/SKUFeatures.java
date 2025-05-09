/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku;

import de.chojo.repbot.config.elements.sku.feature.AnalyzerLog;
import de.chojo.repbot.config.elements.sku.feature.LocaleOverrides;
import de.chojo.repbot.config.elements.sku.feature.ReputationCategories;
import de.chojo.repbot.config.elements.sku.feature.ReputationLog;

public class SKUFeatures {
    ReputationLog reputationLog = new ReputationLog();
    AnalyzerLog analyzerLog = new AnalyzerLog();
    ReputationCategories reputationCategories = new ReputationCategories();
    LocaleOverrides localeOverrides = new LocaleOverrides();

    public ReputationLog reputationLog() {
        return reputationLog;
    }

    public AnalyzerLog analyzerLog() {
        return analyzerLog;
    }

    public ReputationCategories reputationCategories() {
        return reputationCategories;
    }

    public LocaleOverrides localeOverrides() {
        return localeOverrides;
    }
}
