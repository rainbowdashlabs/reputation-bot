/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results.empty;

public enum EmptyResultReason {
    NO_PATTERN("emptyresultreason.nopattern.description"),
    NO_MATCH("emptyresultreason.nomatch.description"),
    REFERENCE_MESSAGE_NOT_FOUND("emptyresultreason.referencemessagenotfound.description"),
    INSUFFICIENT_SCORE("emptyresultreason.insufficientscore.description"),
    INTERNAL_ERROR("emptyresultreason.internalerror.description"),
    TARGET_NOT_ON_GUILD("emptyresultreason.targetnotonguild.description");

    private final String localeKey;

    EmptyResultReason(String localeKey) {
        this.localeKey = localeKey;
    }

    public String localeKey() {
        return localeKey;
    }
}
