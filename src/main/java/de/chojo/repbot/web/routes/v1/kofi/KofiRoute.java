/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.kofi;

import de.chojo.repbot.service.KofiService;
import de.chojo.repbot.service.kofi.KofiTransaction;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;

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

    public void handleKofiPayment(Context ctx) {
        KofiTransaction data = ctx.jsonMapper().fromJsonString(ctx.queryParam("data"), KofiTransaction.class);

        kofiService.handle(data);
    }
}
