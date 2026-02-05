/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.AbuseProtection;
import de.chojo.repbot.dao.access.guild.settings.sub.CooldownDirection;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.AbuseProtectionPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class AbuseProtectionRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update abuse protection settings",
            operationId = "updateAbuseProtectionSettings",
            path = "v1/settings/abuseprotection",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = AbuseProtectionPOJO.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateAbuseSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        AbuseProtection abuseProtection = session.repGuild().settings().abuseProtection();
        AbuseProtectionPOJO abuseProtectionPOJO = ctx.bodyAsClass(AbuseProtectionPOJO.class);
        abuseProtection.apply(abuseProtectionPOJO);
    }

    @OpenApi(
            summary = "Update abuse protection cooldown",
            operationId = "updateAbuseProtectionCooldown",
            path = "v1/settings/abuseprotection/cooldown",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Integer.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateCooldown(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().abuseProtection().cooldown(ctx.bodyAsClass(Integer.class));
    }

    @OpenApi(
            summary = "Update abuse protection cooldown direction",
            operationId = "updateAbuseProtectionCooldownDirection",
            path = "v1/settings/abuseprotection/cooldowndirection",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = CooldownDirection.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateCooldownDirection(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().abuseProtection().cooldownDirection(ctx.bodyAsClass(CooldownDirection.class));
    }

    @OpenApi(
            summary = "Update abuse protection max message age",
            operationId = "updateAbuseProtectionMaxMessageAge",
            path = "v1/settings/abuseprotection/maxmessageage",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Integer.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateMaxMessageAge(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().abuseProtection().maxMessageAge(ctx.bodyAsClass(Integer.class));
    }

    @OpenApi(
            summary = "Update abuse protection min messages",
            operationId = "updateAbuseProtectionMinMessages",
            path = "v1/settings/abuseprotection/minmessages",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Integer.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateMinMessages(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().abuseProtection().minMessages(ctx.bodyAsClass(Integer.class));
    }

    @OpenApi(
            summary = "Update abuse protection donor context",
            operationId = "updateAbuseProtectionDonorContext",
            path = "v1/settings/abuseprotection/donorcontext",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateDonorContext(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().abuseProtection().donorContext(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update abuse protection receiver context",
            operationId = "updateAbuseProtectionReceiverContext",
            path = "v1/settings/abuseprotection/receivercontext",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateReceiverContext(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().abuseProtection().receiverContext(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update abuse protection max given",
            operationId = "updateAbuseProtectionMaxGiven",
            path = "v1/settings/abuseprotection/maxgiven",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Integer.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateMaxGiven(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().abuseProtection().maxGiven(ctx.bodyAsClass(Integer.class));
    }

    @OpenApi(
            summary = "Update abuse protection max given hours",
            operationId = "updateAbuseProtectionMaxGivenHours",
            path = "v1/settings/abuseprotection/maxgivenhours",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Integer.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateMaxGivenHours(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().abuseProtection().maxGivenHours(ctx.bodyAsClass(Integer.class));
    }

    @OpenApi(
            summary = "Update abuse protection max received",
            operationId = "updateAbuseProtectionMaxReceived",
            path = "v1/settings/abuseprotection/maxreceived",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Integer.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateMaxReceived(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().abuseProtection().maxReceived(ctx.bodyAsClass(Integer.class));
    }

    @OpenApi(
            summary = "Update abuse protection max received hours",
            operationId = "updateAbuseProtectionMaxReceivedHours",
            path = "v1/settings/abuseprotection/maxreceivedhours",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Integer.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateMaxReceivedHours(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().abuseProtection().maxReceivedHours(ctx.bodyAsClass(Integer.class));
    }

    @OpenApi(
            summary = "Update abuse protection max message reputation",
            operationId = "updateAbuseProtectionMaxMessageReputation",
            path = "v1/settings/abuseprotection/maxmessagereputation",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Integer.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateMaxMessageReputation(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().abuseProtection().maxMessageReputation(ctx.bodyAsClass(Integer.class));
    }

    @Override
    public void buildRoutes() {
        path("abuseprotection", () -> {
            post("", this::updateAbuseSettings, Role.GUILD_USER);
            post("cooldown", this::updateCooldown, Role.GUILD_USER);
            post("cooldowndirection", this::updateCooldownDirection, Role.GUILD_USER);
            post("maxmessageage", this::updateMaxMessageAge, Role.GUILD_USER);
            post("minmessages", this::updateMinMessages, Role.GUILD_USER);
            post("donorcontext", this::updateDonorContext, Role.GUILD_USER);
            post("receivercontext", this::updateReceiverContext, Role.GUILD_USER);
            post("maxgiven", this::updateMaxGiven, Role.GUILD_USER);
            post("maxgivenhours", this::updateMaxGivenHours, Role.GUILD_USER);
            post("maxreceived", this::updateMaxReceived, Role.GUILD_USER);
            post("maxreceivedhours", this::updateMaxReceivedHours, Role.GUILD_USER);
            post("maxmessagereputation", this::updateMaxMessageReputation, Role.GUILD_USER);
        });
    }
}
