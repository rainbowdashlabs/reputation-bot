/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.dao.components.GuildHolder;
import org.slf4j.Logger;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

public class Gdpr implements GuildHolder {
    private static final Logger log = getLogger(Gdpr.class);
    private final RepGuild repGuild;

    public Gdpr(RepGuild repGuild) {
        this.repGuild = repGuild;
    }

    @Override
    public GuildHolder guildHolder() {
        return repGuild;
    }

    public void queueDeletion() {
        query("""
                INSERT INTO
                    cleanup_schedule(guild_id, user_id, delete_after)
                    VALUES (?, 0, now() + ?::INTERVAL)
                        ON CONFLICT(guild_id, user_id)
                            DO NOTHING;
                """)
                .single(call().bind(guildId())
                        .bind("%d DAYS"
                                .formatted(repGuild.configuration().cleanup().cleanupScheduleDays())))
                .update()
                .ifChanged(i -> {
                    query("UPDATE guilds SET date_left = now() WHERE guild_id = ?");

                    log.debug("Queuing guild {} for deletion.", guildId());
                });
    }

    public void dequeueDeletion() {
        query("DELETE FROM cleanup_schedule WHERE guild_id = ? AND user_id = 0;")
                .single(call().bind(guildId()))
                .update()
                .ifChanged(i -> {
                    query("UPDATE guilds SET date_left = NULL WHERE guild_id = ?")
                            .single(call().bind(guildId()))
                            .update();
                    log.debug("Deletion of guild {} canceled.", guildId());
                });
    }
}
