/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access;

import de.chojo.sadu.queries.api.query.Query;

import java.util.List;

public class Cleanup {
    public List<Long> getCleanupList() {
        return Query.query("SELECT guild_id FROM self_cleanup;")
                    .single()
                    .mapAs(Long.class)
                    .all();
    }
}
