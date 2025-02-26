/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.statistic;

import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Functional interface for appending content to an EmbedBuilder.
 */
@FunctionalInterface
public interface EmbedDisplay {
    /**
     * Appends content to the provided EmbedBuilder.
     *
     * @param embedBuilder the EmbedBuilder to append content to
     */
    void appendTo(EmbedBuilder embedBuilder);
}
