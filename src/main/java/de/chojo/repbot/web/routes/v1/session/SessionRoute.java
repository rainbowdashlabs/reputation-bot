/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.session;

import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.GuildSessionPOJO;
import de.chojo.repbot.web.pojo.guild.GuildMetaPOJO;
import de.chojo.repbot.web.pojo.premium.PremiumFeaturesPOJO;
import de.chojo.repbot.web.pojo.session.UserSessionPOJO;
import de.chojo.repbot.web.pojo.settings.SettingsPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.services.SessionService;
import de.chojo.repbot.web.services.UserSession;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import org.jetbrains.annotations.NotNull;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class SessionRoute implements RoutesBuilder {
    private final SessionService sessionService;

    public SessionRoute(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @OpenApi(
            summary = "Get the data for the current user session.",
            operationId = "getUserSession",
            path = "v1/session/me",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            tags = {"Session"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = UserSessionPOJO.class, type = "application/json")})
            })
    private static void getUserSession(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        ctx.json(session.toPOJO());
    }

    @OpenApi(
            summary = "Get the data for the current guild session.",
            operationId = "getGuildSession",
            path = "v1/session/guild",
            methods = HttpMethod.GET,
            headers = {
                @OpenApiParam(name = "Authorization", required = true, description = "User Session Token"),
                @OpenApiParam(name = "X-Guild-Id", required = true, description = "Guild ID")
            },
            tags = {"Session"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = GuildSessionPOJO.class, type = "application/json")})
            })
    private static void getGuildSession(@NotNull Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ctx.json(session.sessionData());
    }

    @OpenApi(
            summary = "Get the meta data for the current guild.",
            operationId = "getGuildMeta",
            path = "v1/session/guild/meta",
            methods = HttpMethod.GET,
            headers = {
                @OpenApiParam(name = "Authorization", required = true, description = "User Session Token"),
                @OpenApiParam(name = "X-Guild-Id", required = true, description = "Guild ID")
            },
            tags = {"Session"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = GuildMetaPOJO.class, type = "application/json")})
            })
    private static void getGuildMeta(@NotNull Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ctx.json(GuildMetaPOJO.generate(session.guild()));
    }

    @OpenApi(
            summary = "Get the premium data for the current guild.",
            operationId = "getGuildPremium",
            path = "v1/session/guild/premium",
            methods = HttpMethod.GET,
            headers = {
                @OpenApiParam(name = "Authorization", required = true, description = "User Session Token"),
                @OpenApiParam(name = "X-Guild-Id", required = true, description = "Guild ID")
            },
            tags = {"Session"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = PremiumFeaturesPOJO.class, type = "application/json")})
            })
    private static void getGuildPremium(@NotNull Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ctx.json(PremiumFeaturesPOJO.generate(session.repGuild(), session.shardManager()));
    }

    @OpenApi(
            summary = "Get the settings for the current guild.",
            operationId = "getGuildSettings",
            path = "v1/session/guild/settings",
            methods = HttpMethod.GET,
            headers = {
                @OpenApiParam(name = "Authorization", required = true, description = "User Session Token"),
                @OpenApiParam(name = "X-Guild-Id", required = true, description = "Guild ID")
            },
            tags = {"Session"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = SettingsPOJO.class, type = "application/json")})
            })
    private static void getGuildSettings(@NotNull Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ctx.json(SettingsPOJO.generate(session.guild(), session.guildRepository()));
    }

    @Override
    public void buildRoutes() {
        path("session", () -> {
            get("me", SessionRoute::getUserSession, Role.USER);
            path("guild", () -> {
                get(SessionRoute::getGuildSession, Role.GUILD_USER);
                get("meta", SessionRoute::getGuildMeta, Role.GUILD_USER);
                get("premium", SessionRoute::getGuildPremium, Role.GUILD_USER);
                get("settings", SessionRoute::getGuildSettings, Role.GUILD_ADMIN);
            });
        });
    }
}
