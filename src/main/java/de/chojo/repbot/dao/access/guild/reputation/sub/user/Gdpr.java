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

public class Gdpr implements MemberHolder {
    private final RepUser repUser;
    private static final Logger log = getLogger(Gdpr.class);

    public Gdpr(RepUser repUser) {
        this.repUser = repUser;
    }

    @Override
    public Member member() {
        return repUser.member();
    }

    public void queueDeletion() {
        log.debug("User {} is scheduled for deletion on guild {}", userId(), guildId());
        query("""
                   
                INSERT
                   INTO
                       cleanup_schedule(guild_id, user_id, delete_after)
                   VALUES
                       (?, ?, now() + ?::INTERVAL)
                   ON CONFLICT(guild_id, user_id)
                       DO NOTHING;
                   """, repUser.configuration().cleanup().cleanupScheduleDays())
                .single(call().bind(guildId())
                                       .bind(userId())
                                       .bind("%d DAYS".formatted(repUser.configuration().cleanup().cleanupScheduleDays())))
                .update();
    }

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
            log.debug("User {} deletion on guild {} canceled", userId(), guildId());
        }
    }

    @Override
    public User user() {
        return repUser.user();
    }

    @Override
    public Guild guild() {
        return repUser.guild();
    }
}
