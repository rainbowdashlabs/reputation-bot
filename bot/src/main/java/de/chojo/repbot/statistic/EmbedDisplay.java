/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.statistic;

import net.dv8tion.jda.api.EmbedBuilder;

@FunctionalInterface
public interface EmbedDisplay {
    void appendTo(EmbedBuilder embedBuilder);
}
