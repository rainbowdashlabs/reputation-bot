/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.reputation;

import de.chojo.repbot.analyzer.results.AnalyzerResult;
import net.dv8tion.jda.api.entities.Message;

public record ReputationResult(Message message, SubmitResult submitResult, AnalyzerResult analyzerResult) {
    public boolean isSuccess() {
        return submitResult == null && analyzerResult == null;
    }
}
