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

/**
 * Class for handling voice-related data operations.
 */
public class Voice {
    /**
     * Logger for logging events.
     */
    private static final Logger log = getLogger(Voice.class);
    /**
     * Configuration settings.
     */
    private final Configuration configuration;

    /**
     * Constructs a new Voice instance.
     *
     * @param configuration the configuration settings
     */
    public Voice(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Logs that a user was in a channel with other users.
     *
     * @param source the user who saw the other users
     * @param seen the members who were seen by the user
     */
    public void logUser(Member source, List<Member> seen) {
        query("""
                INSERT INTO voice_activity(relation_key, guild_id, user_id_1, user_id_2) VALUES (?,?,?,?)
                    ON CONFLICT(relation_key, guild_id)
                        DO UPDATE
                            SET seen = now()
                """)
                .batch(seen.stream().map(member -> call()
                        .bind(source.getIdLong() ^ member.getIdLong())
                        .bind(source.getGuild().getIdLong())
                        .bind(source.getIdLong())
                        .bind(member.getIdLong())))
                .insert();
    }

    /**
     * Retrieves the last users who were in a voice channel with the requested user in the last minutes.
     *
     * @param user the user to retrieve other users for
     * @param guild the guild to check
     * @param minutes the amount of past minutes
     * @param limit the maximum number of returned IDs
     * @return a list of user IDs
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
                .single(call().bind(guild.getIdLong()).bind(user.getIdLong()).bind(user.getIdLong())
                              .bind(minutes).bind(limit))
                .map(rs -> {
                    var id1 = rs.getLong("user_id_1");
                    var id2 = rs.getLong("user_id_2");
                    return id1 == user.getIdLong() ? id2 : id1;
                }).all();
    }

    /**
     * Cleans up the voice activity data.
     */
    public void cleanup() {
        query("""
                DELETE FROM voice_activity WHERE seen < now() - ?::INTERVAL
                """)
                .single(call().bind("%d hours".formatted(configuration.cleanup().voiceActivityHours())))
                .update();
    }
}
