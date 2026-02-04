/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.dao.components.GuildHolder;
import net.dv8tion.jda.api.entities.Guild;

import java.time.LocalDateTime;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Cleanup implements GuildHolder {
    private final RepGuild repGuild;

    public Cleanup(RepGuild repGuild) {
        this.repGuild = repGuild;
    }

    public void selfCleanupPrompt() {
        query("""
                INSERT INTO self_cleanup(guild_id) VALUES(?)
                """)
                .single(call().bind(guildId()))
                .update();
    }

    public Optional<LocalDateTime> getCleanupPromptTime() {
        return query("""
                SELECT prompted FROM self_cleanup WHERE guild_id = ?
                """)
                .single(call().bind(guildId()))
                .map(rs -> rs.getTimestamp("prompted").toLocalDateTime())
                .first();
    }

    public void cleanupDone() {
        query("""
                DELETE FROM self_cleanup WHERE guild_id = ?
                """)
                .single(call().bind(guildId()))
                .update();
    }

    @Override
    public Guild guild() {
        return repGuild.guild();
    }

    @Override
    public long guildId() {
        return repGuild.guildId();
    }
}
