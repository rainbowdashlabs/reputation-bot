/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reactions.util;

/**
 * Enum representing the result of a check for reactions.
 */
public enum CheckResult {
    /**
     * Indicates that an emoji was found.
     */
    EMOJI_FOUND,

    /**
     * Indicates that an emote was found.
     */
    EMOTE_FOUND,

    /**
     * Indicates that nothing was found.
     */
    NOT_FOUND,

    /**
     * Indicates that an unknown emoji was found.
     */
    UNKNOWN_EMOJI
}
