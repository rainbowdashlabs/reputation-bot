/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub.thanking;

import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.thanking.RolesHolderPOJO;
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

public class DenyReceiverRolesRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update thanking deny receiver roles",
            operationId = "updateThankingDenyReceiverRoles",
            path = "v1/settings/thanking/denyreceiverroles",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = RolesHolderPOJO.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateDenyReceiverRoles(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        RolesHolderPOJO rolesHolderPOJO = ctx.bodyAsClass(RolesHolderPOJO.class);

        // Validate all role IDs
        for (Long roleId : rolesHolderPOJO.roleIds()) {
            session.guildValidator().validateRoleIds(roleId);
        }

        session.repGuild().settings().thanking().denyReceiverRoles().apply(rolesHolderPOJO);
    }

    @Override
    public void buildRoutes() {
        path("denyreceiverroles", () -> {
            post("", this::updateDenyReceiverRoles, Role.GUILD_USER);
        });
    }
}
