/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;

import java.time.LocalDateTime;
import java.util.Optional;

public class Cleanup extends QueryFactory implements GuildHolder {
    private final RepGuild repGuild;

    public Cleanup(RepGuild repGuild) {
        super(repGuild);
        this.repGuild = repGuild;
    }

    public void selfCleanupPrompt() {
        builder()
                .query("""
                       INSERT INTO self_cleanup(guild_id) VALUES(?)
                       """)
                .parameter(stmt -> stmt.setLong(guildId()))
                .update()
                .sendSync();
    }

    public Optional<LocalDateTime> getCleanupPromptTime() {
        return builder(LocalDateTime.class)
                .query("""
                       SELECT prompted FROM self_cleanup WHERE guild_id = ?
                       """)
                .parameter(stmt -> stmt.setLong(guildId()))
                .readRow(rs -> rs.getTimestamp("prompted").toLocalDateTime())
                .firstSync();
    }

    public void cleanupDone() {
        builder(Boolean.class)
                .query("""
                       DELETE FROM self_cleanup WHERE guild_id = ?
                       """)
                .parameter(stmt -> stmt.setLong(guildId()))
                .update()
                .sendSync();
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
