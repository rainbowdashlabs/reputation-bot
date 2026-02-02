/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub.thanking;

import java.util.Collections;
import java.util.List;

public class RanksPOJO {
    protected List<RankEntry> ranks;

    public RanksPOJO() {
    }

    public RanksPOJO(List<RankEntry> ranks) {
        this.ranks = ranks;
    }

    public List<RankEntry> ranks() {
        return Collections.unmodifiableList(ranks);
    }

    public record RankEntry(long roleId, long reputation) {
    }
}
