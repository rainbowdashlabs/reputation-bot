/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.user.sub;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;

import java.sql.SQLException;

import static de.chojo.sadu.queries.api.query.Query.query;

public class UserSettings {
    private final long id;
    private long voteGuild;
    private boolean publicProfile = false;

    public UserSettings(long id, long voteGuild) {
        this.id = id;
        this.voteGuild = voteGuild;
    }

    @MappingProvider({"id", "vote_guild", "public_profile"})
    public UserSettings(Row row) throws SQLException {
        this.id = row.getLong("id");
        this.voteGuild = row.getLong("vote_guild");
        this.publicProfile = row.getBoolean("public_profile");
    }

    public void voteGuild(long voteGuild) {
        query("""
                INSERT
                INTO
                    user_settings(id, vote_guild)
                VALUES
                    (?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    vote_guild = excluded.vote_guild
                """).single(Call.call().bind(id).bind(voteGuild)).insert().ifChanged(i -> this.voteGuild = voteGuild);
    }

    public void publicProfile(boolean publicProfile) {
        query("""
                INSERT
                INTO
                    user_settings(id, public_profile)
                VALUES
                    (?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    public_profile = excluded.public_profile
                """)
                .single(Call.call().bind(id).bind(publicProfile))
                .insert()
                .ifChanged(i -> this.publicProfile = publicProfile);
    }

    public boolean hasVoteGuild() {
        return voteGuild != 0;
    }

    public boolean isPublicProfile() {
        return publicProfile;
    }

    public long id() {
        return id;
    }

    public long voteGuild() {
        return voteGuild;
    }
}
