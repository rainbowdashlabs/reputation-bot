/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.ranking;

import de.chojo.repbot.dao.snapshots.RankingEntry;
import de.chojo.repbot.web.pojo.guild.MemberPOJO;
import io.javalin.openapi.OpenApiName;

/**
 * A ranking entry for the detailed profile, including resolved member information.
 */
@OpenApiName("RankingEntryStat")
public record RankingEntryStatPOJO(long rank, long value, MemberPOJO member) {
    public static RankingEntryStatPOJO generate(RankingEntry entry, MemberPOJO member) {
        return new RankingEntryStatPOJO(entry.rank(), entry.value(), member);
    }
}
