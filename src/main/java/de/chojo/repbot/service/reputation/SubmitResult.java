/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.reputation;

import de.chojo.jdautil.localization.util.Replacement;

import java.util.List;

public record SubmitResult(SubmitResultType type, List<Replacement> replacements) {

    public static SubmitResult of(SubmitResultType type, Replacement... replacements){
        return new SubmitResult(type, List.of(replacements));
    }
}
