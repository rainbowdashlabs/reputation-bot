/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub.thanking;

import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ThankwordsPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import java.util.Arrays;
import java.util.HashSet;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class ThankwordsRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update thanking thankwords settings",
            operationId = "updateThankingThankwordsSettings",
            path = "v1/settings/thanking/thankwords",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ThankwordsPOJO.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateThankwordsSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        var thankwords = session.repGuild().settings().thanking().thankwords();
        var oldValue = new ThankwordsPOJO(thankwords.words());
        ThankwordsPOJO newValue = ctx.bodyAsClass(ThankwordsPOJO.class);
        thankwords.apply(newValue);
        session.recordChange("thanking.thankwords", oldValue, newValue);
    }

    @OpenApi(
            summary = "Update thanking thankwords",
            operationId = "updateThankingThankwordsList",
            path = "v1/settings/thanking/thankwords/words",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = String[].class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateWords(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        var thankwords = session.repGuild().settings().thanking().thankwords();
        var oldValue = thankwords.words();
        ThankwordsPOJO newValue = new ThankwordsPOJO(new HashSet<>(Arrays.asList(ctx.bodyAsClass(String[].class))));
        thankwords.apply(newValue);
        session.recordChange("thanking.thankwords.words", oldValue, newValue.words());
    }

    @Override
    public void buildRoutes() {
        path("thankwords", () -> {
            post("", this::updateThankwordsSettings, Role.GUILD_USER);
            post("words", this::updateWords, Role.GUILD_USER);
        });
    }
}
