/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.autopost.Autopost;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshInterval;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshType;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.service.AutopostService;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.error.ErrorResponseWrapper;
import de.chojo.repbot.web.pojo.settings.sub.AutopostPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
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
    private final AutopostService autopostService;

    public AutopostRoute(AutopostService autopostService) {
        this.autopostService = autopostService;
    }

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
                @OpenApiResponse(
                        status = "403",
                        content = @OpenApiContent(from = ErrorResponseWrapper.class),
                        description = "Premium feature required or limit exceeded")
            })
    public void updateAutopostSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        AutopostPOJO autopostPOJO = ctx.bodyAsClass(AutopostPOJO.class);

        // Validate autopost feature if enabling
        PremiumValidator validator = session.premiumValidator();
        validator.requireFeatureIfEnabled(
                autopostPOJO.active(), validator.features().autopost(), "Autopost");

        Autopost autopost = session.repGuild().settings().autopost();
        AutopostPOJO oldValue = new AutopostPOJO(
                autopost.active(),
                autopost.channelId(),
                autopost.messageId(),
                autopost.refreshType(),
                autopost.refreshInterval());
        autopost.apply(autopostPOJO);
        session.recordChange("autopost", oldValue, autopostPOJO);
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
                @OpenApiResponse(
                        status = "403",
                        content = @OpenApiContent(from = ErrorResponseWrapper.class),
                        description = "Premium feature required or limit exceeded")
            })
    public void updateActive(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        boolean active = ctx.bodyAsClass(Boolean.class);

        // Validate autopost feature if enabling
        PremiumValidator validator = session.premiumValidator();
        validator.requireFeatureIfEnabled(active, validator.features().autopost(), "Autopost");

        Autopost autopost = session.repGuild().settings().autopost();
        boolean oldValue = autopost.active();
        autopost.active(active);
        session.recordChange("autopost.active", oldValue, active);

        if (active) {
            autopostService.update(session.repGuild().guild());
        }
    }

    @OpenApi(
            summary = "Update autopost channel",
            operationId = "updateAutopostChannel",
            path = "v1/settings/autopost/channel",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateChannel(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Long channelId = ctx.bodyAsClass(Long.class);

        // Validate channel ID if not 0 (0 means no channel)
        if (channelId != 0) {
            session.guildValidator().validateChannelIds(channelId);
        }

        Autopost autopost = session.repGuild().settings().autopost();
        long oldValue = autopost.channelId();
        autopost.channel(channelId);
        session.recordChange("autopost.channel", oldValue, channelId);
    }

    @OpenApi(
            summary = "Update autopost refresh type",
            operationId = "updateAutopostRefreshType",
            path = "v1/settings/autopost/refreshtype",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = RefreshType.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateRefreshType(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Autopost autopost = session.repGuild().settings().autopost();
        RefreshType oldValue = autopost.refreshType();
        RefreshType newValue = ctx.bodyAsClass(RefreshType.class);
        autopost.refreshType(newValue);
        session.recordChange("autopost.refreshtype", oldValue, newValue);
    }

    @OpenApi(
            summary = "Update autopost refresh interval",
            operationId = "updateAutopostRefreshInterval",
            path = "v1/settings/autopost/refreshinterval",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = RefreshInterval.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateRefreshInterval(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Autopost autopost = session.repGuild().settings().autopost();
        RefreshInterval oldValue = autopost.refreshInterval();
        RefreshInterval newValue = ctx.bodyAsClass(RefreshInterval.class);
        autopost.refreshInterval(newValue);
        session.recordChange("autopost.refreshinterval", oldValue, newValue);
    }

    @OpenApi(
            summary = "Send autopost now",
            operationId = "sendAutopost",
            path = "v1/settings/autopost/send",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            responses = {@OpenApiResponse(status = "200")})
    public void sendAutopost(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        autopostService.update(session.repGuild());
    }

    @Override
    public void buildRoutes() {
        path("autopost", () -> {
            post("", this::updateAutopostSettings, Role.GUILD_USER);
            post("active", this::updateActive, Role.GUILD_USER);
            post("channel", this::updateChannel, Role.GUILD_USER);
            post("refreshtype", this::updateRefreshType, Role.GUILD_USER);
            post("refreshinterval", this::updateRefreshInterval, Role.GUILD_USER);
            post("send", this::sendAutopost, Role.GUILD_USER);
        });
    }
}
