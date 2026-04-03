/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.ranking;

import de.chojo.repbot.dao.snapshots.RankingEntry;
import de.chojo.repbot.web.pojo.guild.MemberPOJO;
import io.javalin.openapi.OpenApiName;

@OpenApiName("RankingEntry")
public record RankingEntryPOJO(long rank, long value, MemberPOJO member) {
    public static RankingEntryPOJO generate(RankingEntry entry, MemberPOJO member) {
        return new RankingEntryPOJO(entry.rank(), entry.value(), member);
    }
}
