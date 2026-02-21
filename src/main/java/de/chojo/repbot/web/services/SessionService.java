/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.Hashing;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.provider.SettingsAuditLogRepository;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.dao.provider.UserSessionRepository;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.pojo.guild.MemberPOJO;
import de.chojo.repbot.web.pojo.session.GuildSessionData;
import io.javalin.http.Context;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SessionService {
    private static final List<Permission> ADMIN_PERMISSIONS = List.of(
            Permission.ADMINISTRATOR,
            Permission.MANAGE_SERVER,
            Permission.MANAGE_ROLES,
            Permission.KICK_MEMBERS,
            Permission.BAN_MEMBERS,
            Permission.MANAGE_CHANNEL);
    private final Configuration configuration;
    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;
    private final DiscordOAuthService discordOAuthService;
    // A map that maps a token to the current user session.
    private final Cache<String, UserSession> userSessions;
    private final ShardManager shardManager;
    private final GuildRepository guildRepository;
    private final SettingsAuditLogRepository settingsAuditLogRepository;
    // Cache of GuildSession per (userId,guildId)
    private final Cache<GuildSessionKey, GuildSession> guildSessions;
    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    public SessionService(
            Configuration configuration,
            UserSessionRepository userSessionRepository,
            UserRepository userRepository,
            DiscordOAuthService discordOAuthService,
            ShardManager shardManager,
            GuildRepository guildRepository,
            SettingsAuditLogRepository settingsAuditLogRepository) {
        this.configuration = configuration;
        this.userSessionRepository = userSessionRepository;
        this.userRepository = userRepository;
        this.discordOAuthService = discordOAuthService;
        this.shardManager = shardManager;
        this.guildRepository = guildRepository;
        this.settingsAuditLogRepository = settingsAuditLogRepository;
        userSessions = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();
        guildSessions = CacheBuilder.newBuilder()
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build();
    }

    public Optional<UserSession> getUserSession(Context ctx) {
        String authorization = ctx.header("Authorization");
        if (authorization == null) return Optional.empty();
        UserSession session = userSessions.getIfPresent(authorization);
        if (session != null) {
            userSessionRepository.updateLastUsed(authorization);
            return Optional.of(session);
        }
        return tryReconstructSession(authorization);
    }

    private Optional<UserSession> tryReconstructSession(String token) {
        return userSessionRepository.byToken(token).map(meta -> {
            UserSession session = createUserSessionFromToken(meta.userId(), token, meta.created());
            userSessions.put(token, session);
            userSessionRepository.updateLastUsed(token);
            return session;
        });
    }

    public UserSession createSession(long userId) {
        String token = generateToken(userId);
        userSessionRepository.createSession(token, userId);
        UserSession session = createUserSessionFromToken(userId, token, Instant.now());
        userSessions.put(token, session);
        return session;
    }

    private UserSession createUserSessionFromToken(long userId, String token, Instant created) {
        Map<String, GuildSessionData> guilds = new HashMap<>();

        // Build member info for the current user (global, not guild-scoped)
        MemberPOJO memberPojo;
        try {
            User user = shardManager.retrieveUserById(userId).complete();
            if (user != null) {
                memberPojo =
                        new MemberPOJO(user.getName(), String.valueOf(userId), "#ffffff", user.getEffectiveAvatarUrl());
            } else {
                memberPojo = MemberPOJO.generate(String.valueOf(userId));
            }
        } catch (Exception ex) {
            memberPojo = MemberPOJO.generate(String.valueOf(userId));
        }

        var userToken = userRepository.token(userId);
        boolean isBotOwner = configuration.baseSettings().isOwner(userId);
        if (userToken.isEmpty()) {
            log.warn("No discord token found for user {}", userId);
            return new UserSession(userId, token, guilds, memberPojo, created, isBotOwner);
        }

        List<DiscordOAuthService.DiscordGuild> userGuilds;
        try {
            userGuilds = discordOAuthService.getUserGuilds(userToken.get().accessToken());
        } catch (Exception e) {
            log.error("Failed to fetch user guilds for user {}", userId, e);
            return new UserSession(userId, token, guilds, memberPojo, created, isBotOwner);
        }

        for (DiscordOAuthService.DiscordGuild discordGuild : userGuilds) {
            String guildId = discordGuild.id();
            Guild guild = shardManager.getGuildById(guildId);
            if (guild == null) continue;

            Member member;
            try {
                member = guild.retrieveMemberById(userId).complete();
            } catch (Exception e) {
                continue;
            }

            Role role = Role.GUILD_USER;
            if (isBotOwner || ADMIN_PERMISSIONS.stream().anyMatch(member::hasPermission)) {
                role = Role.GUILD_ADMIN;
            }

            guilds.put(
                    guildId,
                    new GuildSessionData(
                            role,
                            discordGuild.id(),
                            discordGuild.name(),
                            discordGuild.icon(),
                            discordGuild.permissions(),
                            discordGuild.permissionsNew(),
                            discordGuild.owner()));
        }

        return new UserSession(userId, token, guilds, memberPojo, created, isBotOwner);
    }

    private Instant tokenInvalidCutoff() {
        return Instant.now().minus(configuration.api().tokenValidHours(), ChronoUnit.HOURS);
    }

    private String generateToken(long userId) {
        var randomString = ThreadLocalRandom.current()
                .ints(10, 'a', 'z')
                .limit(25)
                .mapToObj(Character::toString)
                .collect(Collectors.joining());

        return Hashing.sha256()
                .hashBytes("%s%s".formatted(userId, randomString).getBytes(StandardCharsets.UTF_8))
                .toString();
    }

    public String sessionUrl(long guildId) {
        return pathUrl(guildId, "");
    }

    public String setupUrl(long guildId) {
        return pathUrl(guildId, "setup");
    }

    public String debugUrl(long guildId) {
        return pathUrl(guildId, "settings/problems");
    }

    private String pathUrl(long guildId, String path) {
        String url = "%s/%s".formatted(configuration.api().url(), path);
        return "%s?guild=%s".formatted(url, guildId);
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) return;
        userSessions.invalidate(token);
        userSessionRepository.deleteSession(token);
    }

    public GuildSession getGuildSession(long userId, long guildId) {
        var key = new GuildSessionKey(userId, guildId);
        GuildSession session = guildSessions.getIfPresent(key);
        if (session == null) {
            session = new GuildSession(
                    configuration, shardManager, guildRepository, settingsAuditLogRepository, guildId, userId);
            guildSessions.put(key, session);
        }
        return session;
    }

    public void markGuildDirty(long guildId) {
        guildSessions.asMap().forEach((key, session) -> {
            if (key.guildId == guildId) {
                session.markDirty();
            }
        });
    }

    private record GuildSessionKey(long userId, long guildId) {}
}
