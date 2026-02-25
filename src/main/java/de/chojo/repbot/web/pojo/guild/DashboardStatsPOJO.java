/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild;

import de.chojo.repbot.dao.snapshots.GuildReputationStats;

import java.util.List;

public record DashboardStatsPOJO(
        int totalReputation,
        int weekReputation,
        int todayReputation,
        String topChannelId,
        List<RankingEntryPOJO> topUsers) {
    public static DashboardStatsPOJO generate(GuildReputationStats stats, List<RankingEntryPOJO> topUsers) {
        return new DashboardStatsPOJO(
                stats.totalReputation(),
                stats.weekReputation(),
                stats.todayReputation(),
                String.valueOf(stats.topChannelId()),
                topUsers);
    }
}
