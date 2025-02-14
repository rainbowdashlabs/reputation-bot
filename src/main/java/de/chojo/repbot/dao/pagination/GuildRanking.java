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

/**
 * Class representing a paginated list of guild rankings.
 */
public class GuildRanking extends PageAccess<RepProfile> {
    private final String title;

    /**
     * Constructs a new GuildRanking instance.
     *
     * @param title the title of the guild ranking
     * @param pagecount a supplier providing the total number of pages
     * @param pageSupplier a function providing a list of RepProfile objects for a given page number
     */
    public GuildRanking(String title, Supplier<Integer> pagecount, Function<Integer, List<RepProfile>> pageSupplier) {
        super(pagecount, pageSupplier);
        this.title = title;
    }

    /**
     * Retrieves the title of the guild ranking.
     *
     * @return the title of the guild ranking
     */
    public String title() {
        return title;
    }
}
