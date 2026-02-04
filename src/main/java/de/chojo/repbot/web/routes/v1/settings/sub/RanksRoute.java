/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.thanking.RanksPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.sessions.GuildSession;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class RanksRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Get reputation ranks",
            operationId = "getRanks",
            path = "v1/settings/ranks",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = RanksPOJO.class))
            }
    )
    public void getRanks(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ctx.json(session.repGuild().settings().ranks().toPOJO());
    }

    @OpenApi(
            summary = "Update reputation ranks",
            operationId = "updateRanks",
            path = "v1/settings/ranks",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = RanksPOJO.class)),
            responses = {
                    @OpenApiResponse(status = "200")
            }
    )
    public void updateRanks(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        RanksPOJO ranksPOJO = ctx.bodyAsClass(RanksPOJO.class);
        
        // Validate all role IDs
        for (RanksPOJO.RankEntry rank : ranksPOJO.ranks()) {
            session.guildValidator().validateRoleIds(rank.roleId());
        }
        
        session.repGuild().settings().ranks().apply(ranksPOJO);
    }

    @Override
    public void buildRoutes() {
        path("ranks", () -> {
            get("", this::getRanks, Role.GUILD_USER);
            post("", this::updateRanks, Role.GUILD_USER);
        });
    }
}
