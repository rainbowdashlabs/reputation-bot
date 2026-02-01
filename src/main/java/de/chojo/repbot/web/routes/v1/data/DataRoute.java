/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.data;

import de.chojo.repbot.core.Localization;
import de.chojo.repbot.serialization.ThankwordsContainer;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.pojo.general.LanguageInfo;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class DataRoute implements RoutesBuilder {
    private final ThankwordsContainer thankwordsContainer;
    private final Localization localization;

    public DataRoute(ThankwordsContainer thankwordsContainer, Localization localization) {
        this.thankwordsContainer = thankwordsContainer;
        this.localization = localization;
    }

    @OpenApi(
            summary = "Get default thankwords patterns",
            operationId = "getThankwords",
            path = "v1/data/thankwords",
            methods = HttpMethod.GET,
            tags = {"Data"},
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = ThankwordsContainer.class))
            }
    )
    public void getThankwords(Context ctx) {
        ctx.json(thankwordsContainer);
    }

    @OpenApi(
            summary = "Get available languages",
            operationId = "getLanguages",
            path = "v1/data/languages",
            methods = HttpMethod.GET,
            tags = {"Data"},
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = LanguageInfo[].class))
            }
    )
    public void getLanguages(Context ctx) {
        ctx.json(localization.languages());
    }

    @Override
    public void buildRoutes() {
        path("data", () -> {
            get("thankwords", this::getThankwords, Role.ANYONE);
            get("languages", this::getLanguages, Role.ANYONE);
        });
    }
}
