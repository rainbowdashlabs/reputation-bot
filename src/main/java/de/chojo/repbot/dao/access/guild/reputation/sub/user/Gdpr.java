/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.user;

import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.components.MemberHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Provides GDPR-related data access methods for users.
 */
public class Gdpr implements MemberHolder {
    private final RepUser repUser;
    private static final Logger log = getLogger(Gdpr.class);

    /**
     * Constructs a new Gdpr instance.
     *
     * @param repUser the RepUser instance
     */
    public Gdpr(RepUser repUser) {
        this.repUser = repUser;
    }

    /**
     * Retrieves the member associated with this instance.
     *
     * @return the member
     */
    @Override
    public Member member() {
        return repUser.member();
    }

    /**
     * Queues the user for deletion by adding them to the cleanup schedule.
     */
    public void queueDeletion() {
        log.info("User {} is scheduled for deletion on guild {}", userId(), guildId());
        query("""
                       INSERT INTO
                           cleanup_schedule(guild_id, user_id, delete_after)
                           VALUES (?,?,now() + ?::INTERVAL)
                               ON CONFLICT(guild_id, user_id)
                                   DO NOTHING;
                       """, repUser.configuration().cleanup().cleanupScheduleDays())
                .single(call().bind(guildId())
                                       .bind(userId())
                                       .bind("%d DAYS".formatted(repUser.configuration().cleanup().cleanupScheduleDays())))
                .update();
    }

    /**
     * Dequeues the user from deletion by removing them from the cleanup schedule.
     */
    public void dequeueDeletion() {
        if (query("""
                       DELETE FROM
                           cleanup_schedule
                       WHERE guild_id = ?
                           AND user_id = ?;
                       """)
                .single(call().bind(guildId()).bind(userId()))
                .update()
                .changed()) {
            log.info("User {} deletion on guild {} canceled", userId(), guildId());
        }
    }

    /**
     * Retrieves the user associated with this instance.
     *
     * @return the user
     */
    @Override
    public User user() {
        return repUser.user();
    }

    /**
     * Retrieves the guild associated with this instance.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return repUser.guild();
    }
}
