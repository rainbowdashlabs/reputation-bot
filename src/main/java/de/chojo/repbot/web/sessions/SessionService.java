/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.sessions;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.Hashing;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import io.javalin.http.Context;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.chojo.repbot.util.States.TEST_MODE;

public class SessionService {
    private final Configuration configuration;
    private final Cache<String, GuildSession> guildSessions;
    private final GuildRepository guildRepository;
    private final Map<SessionKey, GuildSession> userSessions = new HashMap<>();

    public SessionService(Configuration configuration, GuildRepository guildRepository) {
        this.configuration = configuration;
        this.guildRepository = guildRepository;
        guildSessions = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .removalListener(notification -> {
                    if (notification.getValue() instanceof GuildSession) {
                        SessionService.this.sessionExpired((GuildSession) notification.getValue());
                    }
                })
                .build();
    }

    public Optional<GuildSession> getGuildSession(Context ctx) {
        String authorization = ctx.header("Authorization");
        if (authorization == null) return Optional.empty();
        return Optional.ofNullable(guildSessions.getIfPresent(authorization));
    }

    /**
     * Get the current session for the given guild and member or create a new one.
     *
     * @param guild  guild
     * @param member member
     * @return existing or new session
     */
    public GuildSession getGuildSession(Guild guild, Member member) {
        return userSessions.computeIfAbsent(SessionKey.from(guild, member), key -> createGuildSession(guild, member));
    }

    private GuildSession createGuildSession(Guild guild, Member member) {
        var randomString = ThreadLocalRandom.current()
                .ints(10, 'a', 'z')
                .limit(25)
                .mapToObj(Character::toString)
                .collect(Collectors.joining());

        String key;
        if (TEST_MODE) {
            key = guild.getId();
        } else {
            key = Hashing.sha256()
                    .hashBytes("%s%s%s"
                            .formatted(guild.getId(), member.getId(), randomString)
                            .getBytes(StandardCharsets.UTF_8))
                    .toString();
        }
        GuildSession session = new GuildSession(
                configuration,
                key,
                guild.getJDA().getShardManager(),
                guildRepository,
                guild.getIdLong(),
                member.getIdLong());
        guildSessions.put(key, session);
        return session;
    }

    private void sessionExpired(GuildSession session) {
        userSessions.remove(SessionKey.from(session));
    }

    record SessionKey(long guildId, long memberId, String key) {
        public static SessionKey from(GuildSession session) {
            return new SessionKey(session.guildId(), session.userId(), null);
        }

        public static SessionKey from(Guild guild, Member member) {
            return new SessionKey(guild.getIdLong(), member.getIdLong(), null);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            SessionKey that = (SessionKey) o;
            return guildId == that.guildId && memberId == that.memberId;
        }

        @Override
        public int hashCode() {
            int result = Long.hashCode(guildId);
            result = 31 * result + Long.hashCode(memberId);
            return result;
        }
    }
}
