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
import org.slf4j.Logger;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.path;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * The Api class initializes and configures the Javalin web server with routes and exception handling.
 */
public class Api {
    private static final Logger log = getLogger(Api.class);
    private final Javalin javalin;
    private final MetricsRoute metricsRoute;

    /**
     * Constructs an Api instance with the specified Javalin instance and Metrics provider.
     *
     * @param javalin the Javalin instance to be used
     * @param metrics the Metrics provider
     */
    public Api(Javalin javalin, Metrics metrics) {
        this.javalin = javalin;
        metricsRoute = new MetricsRoute(metrics);
    }

    /**
     * Initializes the API by setting up exception handling and routes.
     */
    public void init() {
        javalin.exception(ApiException.class, (err, ctx) -> ctx.result(err.getMessage()).status(err.status()));
        javalin.routes(() -> {
            before(ctx -> log.debug("Received request on {}.", ctx.path()));

            path("v1", () -> path("metrics", metricsRoute::buildRoutes));
        });
    }
}
