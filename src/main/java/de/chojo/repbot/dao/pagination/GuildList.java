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

public class GuildList extends PageAccess<RepGuild> {
    public GuildList(Supplier<Integer> pagecount, Function<Integer, List<RepGuild>> pageSupplier) {
        super(pagecount, pageSupplier);
    }
}
