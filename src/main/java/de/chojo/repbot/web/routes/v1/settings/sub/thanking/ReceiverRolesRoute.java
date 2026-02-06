/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub.thanking;

import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.thanking.RolesHolderPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class ReceiverRolesRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update thanking receiver roles",
            operationId = "updateThankingReceiverRoles",
            path = "v1/settings/thanking/receiverroles",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = RolesHolderPOJO.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateReceiverRoles(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        RolesHolderPOJO rolesHolderPOJO = ctx.bodyAsClass(RolesHolderPOJO.class);

        // Validate all role IDs
        for (Long roleId : rolesHolderPOJO.roleIds()) {
            session.guildValidator().validateRoleIds(roleId);
        }

        var receiverRoles = session.repGuild().settings().thanking().receiverRoles();
        var oldValue = receiverRoles.copyRoleIds();
        receiverRoles.apply(rolesHolderPOJO);
        session.recordChange("thanking.receiverroles", oldValue, rolesHolderPOJO.roleIds());
    }

    @Override
    public void buildRoutes() {
        path("receiverroles", () -> {
            post("", this::updateReceiverRoles, Role.GUILD_USER);
        });
    }
}
