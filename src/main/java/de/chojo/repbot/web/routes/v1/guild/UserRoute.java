/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.guild;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.session.GuildSessionData;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.services.RankingService;
import de.chojo.repbot.web.services.UserService;
import de.chojo.repbot.web.services.UserSession;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class UserRoute implements RoutesBuilder {
    private final UserService userService;
    private final UserRankingRoute userRankingRoute;

    public UserRoute(UserService userService, RankingService rankingService, Configuration configuration) {
        this.userService = userService;
        this.userRankingRoute = new UserRankingRoute(rankingService, configuration);
    }

    @Override
    public void buildRoutes() {
        path("/user", () -> {
            path("/profile", () -> {
                get("/me", this::getMyProfile, Role.GUILD_USER);
                get("/{id}", this::getProfile, Role.GUILD_USER);
            });
            userRankingRoute.buildRoutes();
        });
    }

    @OpenApi(
            summary = "Get my profile",
            operationId = "getMyGuildProfile",
            path = "v1/guild/user/profile/me",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Guild"},
            responses = {
                @OpenApiResponse(status = "200"),
                @OpenApiResponse(status = "404", description = "User not found in guild")
            })
    private void getMyProfile(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        UserSession userSession = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        GuildSessionData guildSessionData = userSession.guilds().get(String.valueOf(session.guildId()));
        if (resolveMember(session, session.userId()) == null) {
            ctx.status(404);
            return;
        }
        ctx.json(userService.getProfile(session, session.userId(), guildSessionData));
    }

    @OpenApi(
            summary = "Get profile of a guild member",
            operationId = "getGuildMemberProfile",
            path = "v1/guild/user/profile/{id}",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            pathParams = {@OpenApiParam(name = "id", description = "The user ID of the guild member", required = true)},
            tags = {"Guild"},
            responses = {
                @OpenApiResponse(status = "200"),
                @OpenApiResponse(status = "400", description = "Invalid user ID"),
                @OpenApiResponse(status = "404", description = "User not found in guild")
            })
    private void getProfile(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        UserSession userSession = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        GuildSessionData guildSessionData = userSession.guilds().get(String.valueOf(session.guildId()));

        long targetUserId;
        try {
            targetUserId = Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400);
            return;
        }

        if (resolveMember(session, targetUserId) == null) {
            ctx.status(404);
            return;
        }
        ctx.json(userService.getProfile(session, targetUserId, guildSessionData));
    }

    private @Nullable Member resolveMember(GuildSession session, long userId) {
        try {
            return session.guild().retrieveMemberById(userId).complete();
        } catch (Exception ignore) {
            return null;
        }
    }
}
