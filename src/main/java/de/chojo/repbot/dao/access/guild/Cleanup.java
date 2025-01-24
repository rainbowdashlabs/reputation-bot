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

/**
 * Class responsible for handling cleanup operations for a guild.
 */
public class Cleanup implements GuildHolder {
    private final RepGuild repGuild;

    /**
     * Constructs a Cleanup instance with the specified RepGuild.
     *
     * @param repGuild the RepGuild instance
     */
    public Cleanup(RepGuild repGuild) {
        this.repGuild = repGuild;
    }

    /**
     * Prompts self-cleanup by inserting a record into the self_cleanup table.
     */
    public void selfCleanupPrompt() {
        query("""
                       INSERT INTO self_cleanup(guild_id) VALUES(?)
                       """)
                .single(call().bind(guildId()))
                .update();
    }

    /**
     * Retrieves the time when the cleanup was prompted.
     *
     * @return an Optional containing the LocalDateTime of the prompt, if present
     */
    public Optional<LocalDateTime> getCleanupPromptTime() {
        return query("""
                       SELECT prompted FROM self_cleanup WHERE guild_id = ?
                       """)
                .single(call().bind(guildId()))
                .map(rs -> rs.getTimestamp("prompted").toLocalDateTime())
                .first();
    }

    /**
     * Marks the cleanup as done by deleting the record from the self_cleanup table.
     */
    public void cleanupDone() {
        query("""
                       DELETE FROM self_cleanup WHERE guild_id = ?
                       """)
                .single(call().bind(guildId()))
                .update();
    }

    /**
     * Gets the Guild associated with this Cleanup instance.
     *
     * @return the Guild
     */
    @Override
    public Guild guild() {
        return repGuild.guild();
    }

    /**
     * Gets the ID of the Guild associated with this Cleanup instance.
     *
     * @return the Guild ID
     */
    @Override
    public long guildId() {
        return repGuild.guildId();
    }
}
