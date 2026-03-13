/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access;

import java.util.List;

import static de.chojo.sadu.queries.api.query.Query.query;

public class Cleanup {
    public List<Long> getCleanupList() {
        return query("SELECT guild_id FROM self_cleanup;")
                .single()
                .mapAs(Long.class)
                .all();
    }
}
