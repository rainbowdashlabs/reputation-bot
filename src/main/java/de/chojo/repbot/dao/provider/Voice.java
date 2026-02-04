/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.provider;

import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

public class Voice {
    private static final Logger log = getLogger(Voice.class);
    private final Configuration configuration;

    public Voice(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Log that a user was in a channel with another user.
     *
     * @param source the user which has seen the users.
     * @param seen   the members which were seen by the user lately
     */
    public void logUser(Member source, List<Member> seen) {
        query("""
                INSERT INTO voice_activity(relation_key, guild_id, user_id_1, user_id_2) VALUES (?,?,?,?)
                    ON CONFLICT(relation_key, guild_id)
                        DO UPDATE
                            SET seen = now()
                """)
                .batch(seen.stream().map(member -> call().bind(source.getIdLong() ^ member.getIdLong())
                        .bind(source.getGuild().getIdLong())
                        .bind(source.getIdLong())
                        .bind(member.getIdLong())))
                .insert();
    }

    /**
     * Retrieve the last users which were in a voice channel with the requested user in the last minutes.
     *
     * @param user    user to retrieve other users for
     * @param guild   guild to check
     * @param minutes the amount of past minutes
     * @param limit   max number of returned ids
     * @return list of ids
     */
    public List<Long> getPastUser(User user, Guild guild, int minutes, int limit) {
        return query("""
                SELECT
                    user_id_1, user_id_2
                FROM
                    voice_activity
                WHERE
                 guild_id = ?
                 AND (user_id_1 = ?
                    OR user_id_2 = ?
                 )
                 AND seen > now() - (? || 'minute')::INTERVAL
                ORDER BY
                    seen DESC
                LIMIT ?;
                """)
                .single(call().bind(guild.getIdLong())
                        .bind(user.getIdLong())
                        .bind(user.getIdLong())
                        .bind(minutes)
                        .bind(limit))
                .map(rs -> {
                    var id1 = rs.getLong("user_id_1");
                    var id2 = rs.getLong("user_id_2");
                    return id1 == user.getIdLong() ? id2 : id1;
                })
                .all();
    }

    /**
     * Cleanup the voice activity
     */
    public void cleanup() {
        query("""
                DELETE FROM voice_activity WHERE seen < now() - ?::INTERVAL
                """)
                .single(call().bind("%d hours".formatted(configuration.cleanup().voiceActivityHours())))
                .update();
    }
}
