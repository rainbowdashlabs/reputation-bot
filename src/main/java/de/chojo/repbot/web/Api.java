/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.web.error.ApiException;
import de.chojo.repbot.web.routes.v1.MetricsRoute;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.router.Endpoint;
import org.slf4j.Logger;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.path;
import static org.slf4j.LoggerFactory.getLogger;

public class Api {
    private static final Logger log = getLogger(Api.class);
    private final MetricsRoute metricsRoute;

    public Api(Metrics metrics) {
        metricsRoute = new MetricsRoute(metrics);
    }

    public void init() {
        before(ctx -> log.debug("Received request on {}.", ctx.path()));
        path("v1", () -> path("metrics", metricsRoute::buildRoutes));
    }
}
