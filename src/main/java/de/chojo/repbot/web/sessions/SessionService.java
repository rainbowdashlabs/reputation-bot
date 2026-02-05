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
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.dao.access.guildsession.GuildSessionMeta;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.provider.GuildSessionRepository;
import io.javalin.http.Context;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.chojo.repbot.util.States.TEST_MODE;

public class SessionService {
    private final Configuration configuration;
    // A map that maps a token to the current session.
    // The session key is only stored inside the session and is not equal to the token
    private final Cache<String, GuildSession> guildSessions;
    private final GuildSessionRepository guildSessionRepository;
    private final GuildRepository guildRepository;
    private final Map<SessionKey, GuildSession> userSessions = new HashMap<>();
    private final ShardManager shardManager;

    public SessionService(Configuration configuration, GuildSessionRepository guildSessionRepository, GuildRepository guildRepository, ShardManager shardManager) {
        this.configuration = configuration;
        this.guildSessionRepository = guildSessionRepository;
        this.guildRepository = guildRepository;
        this.shardManager = shardManager;
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
        // check whether there is an open session with this token already
        GuildSession session = guildSessions.getIfPresent(authorization);
        if (session != null) return Optional.of(session);
        // If not, check whether there is a session that we can reconstruct from the token
        return tryReconstructSession(authorization);
    }

    private Optional<GuildSession> tryReconstructSession(String token) {
        Optional<GuildSessionMeta> optMeta = guildSessionRepository.byToken(token);
        if (optMeta.isEmpty()) return Optional.empty();
        GuildSessionMeta guildSessionMeta = optMeta.get();
        if (guildSessionMeta.created().isBefore(tokenInvalidCutoff())) {
            // Session is expired, we delete it. It should no longer be used.
            guildSessionMeta.delete();
            return Optional.empty();
        }
        GuildSession session = guildSessionMeta.toGuildSession(configuration, shardManager, guildRepository);
        guildSessions.put(token, session);
        return Optional.of(session);
    }

    private GuildSession tryRecreateSession(Guild guild, Member member) {
        Optional<GuildSessionMeta> optMeta = guildSessionRepository.byGuildAndMemer(guild, member);
        // There is still a valid session
        if (optMeta.isPresent()) {
            GuildSessionMeta guildSessionMeta = optMeta.get();
            if (guildSessionMeta.created().isBefore(tokenInvalidCutoff())) {
                // Session is expired, we delete it. It should no longer be used.
                guildSessionMeta.delete();
                return tryRecreateSession(guild, member);
            }
            return optMeta.get().toGuildSession(configuration, shardManager, guildRepository);
        }

        return createGuildSession(guild, member);
    }

    private Instant tokenInvalidCutoff() {
        return Instant.now().minus(configuration.api().tokenValidHours(), ChronoUnit.HOURS);
    }

    /**
     * Get the current session for the given guild and member or create a new one.
     *
     * @param guild  guild
     * @param member member
     * @return existing or new session
     */
    public GuildSession getGuildSession(Guild guild, Member member) {
        return userSessions.computeIfAbsent(SessionKey.from(guild, member), key -> tryRecreateSession(guild, member));
    }

    private GuildSession createGuildSession(Guild guild, Member member) {
        String token = generateToken(guild.getIdLong(), member.getIdLong());
        GuildSessionMeta newSession = guildSessionRepository.createNewSession(token, guild.getIdLong(), member.getIdLong());
        GuildSession session = newSession.toGuildSession(configuration, shardManager, guildRepository);
        guildSessions.put(token, session);
        return session;
    }

    private String generateToken(long guild, long member) {
        if (TEST_MODE) return "test_mode%stestmode".formatted(guild);

        var randomString = ThreadLocalRandom.current()
                                            .ints(10, 'a', 'z')
                                            .limit(25)
                                            .mapToObj(Character::toString)
                                            .collect(Collectors.joining());

        return Hashing.sha256()
                      .hashBytes("%s%s%s"
                              .formatted(guild, member, randomString)
                              .getBytes(StandardCharsets.UTF_8))
                      .toString();
    }

    public void invalidateMemberSession(Member member) {
        Optional<GuildSessionMeta> guildSessionMeta = guildSessionRepository.byGuildAndMemer(member.getGuild(), member);
    }

    private void sessionExpired(GuildSession session) {
        userSessions.remove(SessionKey.from(session));
        session.meta().used();
    }

    record SessionKey(long guildId, long memberId, String key) {
        public static SessionKey from(GuildSession session) {
            return new SessionKey(session.guildId(), session.memberId(), null);
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
