/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

/**
 * Utility class for emoji debug symbols.
 */
public final class EmojiDebug {
    /**
     * Indicates that a thank word was found.
     */
    public static final String FOUND_THANKWORD = "👀";

    /**
     * Indicates that only a cooldown is active.
     */
    public static final String ONLY_COOLDOWN = "💤";

    /**
     * Indicates that the context is empty.
     */
    public static final String EMPTY_CONTEXT = "🔍";

    /**
     * Indicates that the target is not in the context.
     */
    public static final String TARGET_NOT_IN_CONTEXT = "❓";

    /**
     * Indicates that the donor is not in the context.
     */
    public static final String DONOR_NOT_IN_CONTEXT = "❔";

    /**
     * Indicates that the context is too old.
     */
    public static final String TOO_OLD = "🕛";

    /**
     * Indicates that a prompt was made.
     */
    public static final String PROMPTED = "🗨";

    /**
     * Indicates that the receiver limit was reached.
     */
    public static final String RECEIVER_LIMIT = "✋";

    /**
     * Indicates that the donor limit was reached.
     */
    public static final String DONOR_LIMIT = "🤲";

    /**
     * Private constructor to prevent instantiation.
     * Throws an UnsupportedOperationException if called.
     */
    private EmojiDebug() {
        throw new UnsupportedOperationException("This is a utility class.");
    }
}
