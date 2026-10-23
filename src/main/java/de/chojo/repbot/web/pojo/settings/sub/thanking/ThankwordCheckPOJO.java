/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub.thanking;

import java.util.List;

/**
 * The result of checking a text for thank words.
 *
 * @param matched    whether a thank word was found
 * @param match      the part of the text that was recognised as a thank word, or null if none was found
 * @param thankwords the configured thank words that match {@code match}, empty if none was found
 */
public record ThankwordCheckPOJO(boolean matched, String match, List<String> thankwords) {
    /**
     * The result for a text without a thank word.
     *
     * @return a result without a match
     */
    public static ThankwordCheckPOJO noMatch() {
        return new ThankwordCheckPOJO(false, null, List.of());
    }
}
