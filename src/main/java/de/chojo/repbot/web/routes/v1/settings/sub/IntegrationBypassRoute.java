/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.IntegrationBypass;
import de.chojo.repbot.dao.access.guild.settings.sub.integrationbypass.Bypass;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class IntegrationBypassRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update integration bypass settings",
            operationId = "updateIntegrationBypassSettings",
            path = "v1/settings/integrationbypass",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Bypass.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateBypass(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Bypass bypass = ctx.bodyAsClass(Bypass.class);
        IntegrationBypass integrationBypass = session.repGuild().settings().integrationBypass();
        
        Bypass oldValue = integrationBypass.getBypass(bypass.integrationId()).orElse(null);
        integrationBypass.apply(bypass);
        session.recordChange("integration_bypass." + bypass.integrationId(), oldValue, bypass);
    }

    @OpenApi(
            summary = "Delete integration bypass settings",
            operationId = "deleteIntegrationBypassSettings",
            path = "v1/settings/integrationbypass/{integrationId}",
            methods = HttpMethod.DELETE,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            pathParams = {@OpenApiParam(name = "integrationId", required = true, description = "The integration id", type = Long.class)},
            tags = {"Settings"},
            responses = {@OpenApiResponse(status = "200")})
    public void deleteBypass(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        long integrationId = ctx.pathParamAsClass("integrationId", Long.class).get();
        IntegrationBypass integrationBypass = session.repGuild().settings().integrationBypass();

        Bypass oldValue = integrationBypass.getBypass(integrationId).orElse(null);
        integrationBypass.remove(integrationId);
        session.recordChange("integration_bypass." + integrationId, oldValue, null);
    }

    @Override
    public void buildRoutes() {
        path("integrationbypass", () -> {
            post("", this::updateBypass, Role.GUILD_USER);
            delete("{integrationId}", this::deleteBypass, Role.GUILD_USER);
        });
    }
}
