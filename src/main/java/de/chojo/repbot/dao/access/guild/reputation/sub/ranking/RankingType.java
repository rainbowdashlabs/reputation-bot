/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.ranking;

public enum RankingType {
    RECEIVED,
    GIVEN;

    private final String localeKey;

    RankingType() {
        localeKey = "ranking." + name().toLowerCase();
    }

    public String localeKey() {
        return localeKey;
    }
}
