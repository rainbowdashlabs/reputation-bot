/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results.empty;

/**
 * Enum representing the reasons for an empty result.
 */
public enum EmptyResultReason {
    /**
     * No pattern was found.
     */
    NO_PATTERN("emptyresultreason.nopattern.description"),

    /**
     * No match was found.
     */
    NO_MATCH("emptyresultreason.nomatch.description"),

    /**
     * The reference message was not found.
     */
    REFERENCE_MESSAGE_NOT_FOUND("emptyresultreason.referencemessagenotfound.description"),

    /**
     * The score was insufficient.
     */
    INSUFFICIENT_SCORE("emptyresultreason.insufficientscore.description"),

    /**
     * An internal error occurred.
     */
    INTERNAL_ERROR("emptyresultreason.internalerror.description"),

    /**
     * The target is not on the guild.
     */
    TARGET_NOT_ON_GUILD("emptyresultreason.targetnotonguild.description");

    private final String localeKey;

    /**
     * Constructs an EmptyResultReason with the specified locale key.
     *
     * @param localeKey the locale key for the reason
     */
    EmptyResultReason(String localeKey) {
        this.localeKey = localeKey;
    }

    /**
     * Retrieves the locale key for the reason.
     *
     * @return the locale key
     */
    public String localeKey() {
        return localeKey;
    }
}
