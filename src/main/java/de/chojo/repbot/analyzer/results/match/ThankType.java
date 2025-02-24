/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results.match;

/**
 * Enum representing different types of thank actions.
 */
public enum ThankType {
    /**
     * Fuzzy thank type.
     */
    FUZZY("thankType.fuzzy.name"),

    /**
     * Mention thank type.
     */
    MENTION("thankType.mention.name"),

    /**
     * Answer thank type.
     */
    ANSWER("thankType.answer.name"),

    /**
     * Direct thank type.
     */
    DIRECT("thankType.direct.name"),

    /**
     * Reaction thank type.
     */
    REACTION("thankType.reaction.name"),

    /**
     * Embed thank type.
     */
    EMBED("thankType.embed.name");

    private final String nameLocaleKey;

    /**
     * Constructs a new ThankType with the given locale key.
     *
     * @param nameLocaleKey the locale key associated with the thank type
     */
    ThankType(String nameLocaleKey) {
        this.nameLocaleKey = nameLocaleKey;
    }

    /**
     * Retrieves the locale key associated with the thank type.
     *
     * @return the locale key
     */
    public String nameLocaleKey() {
        return nameLocaleKey;
    }
}
