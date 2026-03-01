/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.web.routes.v1.metrics.util.MetricCache;
import de.chojo.repbot.web.routes.v1.metrics.util.MetricLimits;
import de.chojo.repbot.web.routes.v1.metrics.util.MetricsHolder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Limits extends MetricsHolder {
    public Limits(Metrics metrics, MetricCache cache) {
        super(cache, metrics);
    }

    @OpenApi(
            summary = "Get the limits for metric query parameters.",
            operationId = "metricLimits",
            path = "v1/metrics/limits",
            methods = HttpMethod.GET,
            tags = {"Metrics"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = MetricLimits.class, type = "application/json")})
            })
    public void limits(Context ctx) {
        ctx.json(MetricLimits.fromConstants());
    }

    @Override
    public void buildRoutes() {
        path("limits", () -> get(this::limits));
    }
}
