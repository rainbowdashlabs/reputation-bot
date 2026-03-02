/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.kofi;

import de.chojo.repbot.service.KofiService;
import de.chojo.repbot.service.kofi.KofiTransaction;
import de.chojo.repbot.util.Urls;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class KofiRoute implements RoutesBuilder {
    private final KofiService kofiService;

    public KofiRoute(KofiService kofiService) {
        this.kofiService = kofiService;
    }

    @Override
    public void buildRoutes() {
        path("kofi", () -> {
            post(this::handleKofiPayment);
        });
    }

    @OpenApi(
            summary = "Handle Ko-fi payment webhook",
            operationId = "handleKofiPayment",
            path = "v1/kofi",
            methods = HttpMethod.POST,
            tags = {"Kofi"},
            queryParams = {@OpenApiParam(name = "data", required = true, description = "JSON encoded KofiTransaction")},
            responses = {@OpenApiResponse(status = "200", description = "OK")})
    public void handleKofiPayment(Context ctx) {
        var results = Urls.splitQuery(ctx.body());
        var json = results.get("data");
        KofiTransaction data = ctx.jsonMapper().fromJsonString(json, KofiTransaction.class);

        kofiService.handle(data);
        ctx.status(200);
    }
}
