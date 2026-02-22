/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.user;

import de.chojo.repbot.dao.access.user.UserSettings;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.user.UserSettingsPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.services.UserSession;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import org.jetbrains.annotations.NotNull;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.patch;

public class UserSettingsRoute implements RoutesBuilder {
    private final UserRepository userRepository;

    public UserSettingsRoute(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @OpenApi(
            summary = "Get the current user settings.",
            operationId = "getUserSettings",
            path = "v1/user/settings",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            tags = {"User"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = UserSettingsPOJO.class, type = "application/json")})
            })
    private void getUserSettings(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        UserSettings settings = userRepository.getSettingsById(session.userId());
        ctx.json(new UserSettingsPOJO(settings.voteGuild()));
    }

    @OpenApi(
            summary = "Update the user settings.",
            operationId = "updateUserSettings",
            path = "v1/user/settings",
            methods = HttpMethod.PATCH,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = UserSettingsPOJO.class)),
            tags = {"User"},
            responses = {@OpenApiResponse(status = "204")})
    private void updateUserSettings(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        UserSettingsPOJO body = ctx.bodyAsClass(UserSettingsPOJO.class);

        if (body.voteGuild() != 0 && !session.guilds().containsKey(String.valueOf(body.voteGuild()))) {
            ctx.status(400).result("Guild not in session");
            return;
        }

        UserSettings settings = userRepository.getSettingsById(session.userId());
        settings.voteGuild(body.voteGuild());
        ctx.status(204);
    }

    @Override
    public void buildRoutes() {
        get(this::getUserSettings, Role.USER);
        patch(this::updateUserSettings, Role.USER);
    }
}
