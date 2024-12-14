/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.pagination;

import de.chojo.repbot.dao.access.guild.RepGuild;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class representing a paginated list of guilds.
 */
public class GuildList extends PageAccess<RepGuild> {

    /**
     * Constructs a new GuildList instance.
     *
     * @param pagecount a supplier providing the total number of pages
     * @param pageSupplier a function providing a list of RepGuild objects for a given page number
     */
    public GuildList(Supplier<Integer> pagecount, Function<Integer, List<RepGuild>> pageSupplier) {
        super(pagecount, pageSupplier);
    }
}
