/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access;

import de.chojo.sadu.base.QueryFactory;

import javax.sql.DataSource;
import java.util.List;

public class Cleanup extends QueryFactory {
    public Cleanup(DataSource dataSource) {
        super(dataSource);
    }

    public List<Long> getCleanupList() {
        return builder(Long.class)
                .queryWithoutParams("""
                                                SELECT guild_id FROM self_cleanup;
                                    """)
                .readRow(stmt -> stmt.getLong("guild_id"))
                .allSync();
    }
}
