/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.pagination;

import de.chojo.repbot.dao.snapshots.RepProfile;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuildRanking extends PageAccess<RepProfile> {
    private final String title;

    public GuildRanking(String title, Supplier<Integer> pagecount, Function<Integer, List<RepProfile>> pageSupplier) {
        super(pagecount, pageSupplier);
        this.title = title;
    }

    public String title() {
        return title;
    }
}
