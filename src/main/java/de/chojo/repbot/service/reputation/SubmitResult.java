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

/**
 * Record representing the result of a submission.
 *
 * @param type the type of the submission result
 * @param replacements the list of replacements associated with the submission result
 */
public record SubmitResult(SubmitResultType type, List<Replacement> replacements) {

    /**
     * Creates a new SubmitResult instance with the given type and replacements.
     *
     * @param type the type of the submission result
     * @param replacements the array of replacements associated with the submission result
     * @return a new SubmitResult instance
     */
    public static SubmitResult of(SubmitResultType type, Replacement... replacements){
        return of(type, List.of(replacements));
    }

    /**
     * Creates a new SubmitResult instance with the given type and replacements.
     *
     * @param type the type of the submission result
     * @param replacements the list of replacements associated with the submission result
     * @return a new SubmitResult instance
     */
    @JsonCreator
    public static SubmitResult of(@JsonProperty("type") SubmitResultType type,  @JsonProperty("replacements") List<Replacement> replacements){
        return new SubmitResult(type, replacements);
    }
}
