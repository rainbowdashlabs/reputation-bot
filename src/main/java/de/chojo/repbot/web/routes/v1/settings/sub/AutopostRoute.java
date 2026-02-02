/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.autopost.Autopost;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshInterval;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshType;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.error.ErrorResponse;
import de.chojo.repbot.web.pojo.settings.sub.AutopostPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.sessions.GuildSession;
import de.chojo.repbot.web.validation.PremiumValidator;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class AutopostRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update autopost settings",
            operationId = "updateAutopostSettings",
            path = "v1/settings/autopost",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = AutopostPOJO.class)),
            responses = {
                    @OpenApiResponse(status = "200"),
                    @OpenApiResponse(status = "403", content = @OpenApiContent(from = ErrorResponse.class), description = "Premium feature required or limit exceeded")
            }
    )
    public void updateAutopostSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        AutopostPOJO autopostPOJO = ctx.bodyAsClass(AutopostPOJO.class);

        // Validate autopost feature if enabling
        PremiumValidator validator = session.premiumValidator();
        validator.requireFeatureIfEnabled(autopostPOJO.active(), validator.features().autopost(), "Autopost");

        Autopost autopost = session.repGuild().settings().autopost();
        autopost.apply(autopostPOJO);
    }

    @OpenApi(
            summary = "Update autopost active",
            operationId = "updateAutopostActive",
            path = "v1/settings/autopost/active",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {
                    @OpenApiResponse(status = "200"),
                    @OpenApiResponse(status = "403", content = @OpenApiContent(from = ErrorResponse.class), description = "Premium feature required or limit exceeded")
            }
    )
    public void updateActive(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        boolean active = ctx.bodyAsClass(Boolean.class);

        // Validate autopost feature if enabling
        PremiumValidator validator = session.premiumValidator();
        validator.requireFeatureIfEnabled(active, validator.features().autopost(), "Autopost");

        session.repGuild().settings().autopost().active(active);
    }

    @OpenApi(
            summary = "Update autopost channel",
            operationId = "updateAutopostChannel",
            path = "v1/settings/autopost/channel",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateChannel(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().autopost().channel(ctx.bodyAsClass(Long.class));
    }

    @OpenApi(
            summary = "Update autopost message",
            operationId = "updateAutopostMessage",
            path = "v1/settings/autopost/message",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateMessage(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().autopost().message(ctx.bodyAsClass(Long.class));
    }

    @OpenApi(
            summary = "Update autopost refresh type",
            operationId = "updateAutopostRefreshType",
            path = "v1/settings/autopost/refreshtype",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = RefreshType.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateRefreshType(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().autopost().refreshType(ctx.bodyAsClass(RefreshType.class));
    }

    @OpenApi(
            summary = "Update autopost refresh interval",
            operationId = "updateAutopostRefreshInterval",
            path = "v1/settings/autopost/refreshinterval",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = RefreshInterval.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateRefreshInterval(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().autopost().refreshInterval(ctx.bodyAsClass(RefreshInterval.class));
    }

    @Override
    public void buildRoutes() {
        path("autopost", () -> {
            post("", this::updateAutopostSettings, Role.GUILD_USER);
            post("active", this::updateActive, Role.GUILD_USER);
            post("channel", this::updateChannel, Role.GUILD_USER);
            post("message", this::updateMessage, Role.GUILD_USER);
            post("refreshtype", this::updateRefreshType, Role.GUILD_USER);
            post("refreshinterval", this::updateRefreshInterval, Role.GUILD_USER);
        });
    }
}
