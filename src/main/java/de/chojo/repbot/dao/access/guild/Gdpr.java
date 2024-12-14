/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.dao.components.GuildHolder;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Provides GDPR-related data access methods for guilds.
 */
public class Gdpr implements GuildHolder {
    private final RepGuild repGuild;
    private static final Logger log = getLogger(Gdpr.class);

    /**
     * Constructs a new Gdpr instance.
     *
     * @param repGuild the RepGuild instance
     */
    public Gdpr(RepGuild repGuild) {
        this.repGuild = repGuild;
    }

    /**
     * Retrieves the guild associated with this instance.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return repGuild.guild();
    }

    /**
     * Retrieves the ID of the guild associated with this instance.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return repGuild.guildId();
    }

    /**
     * Queues the guild for deletion by adding it to the cleanup schedule.
     */
    public void queueDeletion() {
        if (query("""
                INSERT INTO
                    cleanup_schedule(guild_id, user_id, delete_after)
                    VALUES (?, 0, now() + ?::INTERVAL)
                        ON CONFLICT(guild_id, user_id)
                            DO NOTHING;
                """)
                .single(call().bind(guildId()).bind("%d DAYS".formatted(repGuild.configuration().cleanup().cleanupScheduleDays())))
                .update()
                .changed()) {
            log.debug("Queuing guild {} for deletion.", guildId());
        }
    }

    /**
     * Dequeues the guild from deletion by removing it from the cleanup schedule.
     */
    public void dequeueDeletion() {
        if (query("DELETE FROM cleanup_schedule WHERE guild_id = ? AND user_id = 0;")
                .single(call().bind(guildId()))
                .update()
                .changed()) {
            log.debug("Deletion of guild {} canceled.", guildId());
        }
    }
}
