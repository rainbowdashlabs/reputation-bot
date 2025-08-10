/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.reputation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.chojo.jdautil.localization.util.Replacement;

import java.util.List;

public record SubmitResult(SubmitResultType type, List<Replacement> replacements) {

    public static SubmitResult of(SubmitResultType type, Replacement... replacements) {
        return of(type, List.of(replacements));
    }

    @JsonCreator
    public static SubmitResult of(@JsonProperty("type") SubmitResultType type, @JsonProperty("replacements") List<Replacement> replacements) {
        return new SubmitResult(type, replacements);
    }
}
