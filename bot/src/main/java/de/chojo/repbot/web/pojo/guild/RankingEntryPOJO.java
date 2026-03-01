/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild;

import de.chojo.repbot.dao.snapshots.RankingEntry;

public record RankingEntryPOJO(long rank, MemberPOJO member, long value) {
    public static RankingEntryPOJO generate(RankingEntry entry, MemberPOJO member) {
        return new RankingEntryPOJO(entry.rank(), member, entry.value());
    }
}
