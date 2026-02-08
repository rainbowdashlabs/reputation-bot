/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.DebugService;
import de.chojo.repbot.service.debugService.DebugResult;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class DebugRoute implements RoutesBuilder {

    private final DebugService debugService;

    public DebugRoute(GuildRepository guildRepository) {
        this.debugService = new DebugService(guildRepository);
    }

    @Override
    public void buildRoutes() {
        path("debug", () -> {
            get("", this::getDebug, Role.GUILD_USER);
        });
    }

    @OpenApi(
            summary = "Get debug information for the guild",
            operationId = "getDebug",
            path = "v1/settings/debug",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            responses = {
                @OpenApiResponse(status = "200", content = @io.javalin.openapi.OpenApiContent(from = DebugResult.class))
            })
    public void getDebug(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ctx.json(debugService.debug(session.guild()));
    }
}
