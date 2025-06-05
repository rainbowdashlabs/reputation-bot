/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.pagination;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.dao.snapshots.RankingEntry;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class Ranking extends PageAccess<RankingEntry> {
    private final String title;
    private final Replacement replacement;

    public Ranking(String title, Replacement replacement, Supplier<Integer> pagecount, Function<Integer, List<RankingEntry>> pageSupplier) {
        super(pagecount, pageSupplier);
        this.title = title;
        this.replacement = replacement;
    }

    public String title() {
        return title;
    }

    public Replacement replacement() {
        return replacement;
    }
}
