/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.user;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;

import java.sql.SQLException;

public class UserSettings {
    private final long id;
    private long voteGuild;

    public UserSettings(long id, long voteGuild) {
        this.id = id;
        this.voteGuild = voteGuild;
    }

    @MappingProvider({"id", "vote_guild"})
    public UserSettings(Row row) throws SQLException {
        this.id = row.getLong("id");
        this.voteGuild = row.getLong("vote_guild");
    }

    public void voteGuild(long voteGuild) {
        if (Query.query("""
                         INSERT INTO user_settings(id, vote_guild) VALUES(?, ?) ON CONFLICT(id) DO UPDATE SET vote_guild = excluded.vote_guild
                         """)
                .single(Call.call().bind(id).bind(voteGuild))
                .insert()
                .changed()) {
            this.voteGuild = voteGuild;
        }
    }

    public boolean hasVoteGuild() {
        return voteGuild != 0;
    }

    public long id() {
        return id;
    }

    public long voteGuild() {
        return voteGuild;
    }
}
