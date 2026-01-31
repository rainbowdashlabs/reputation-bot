/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.UsersStatistic;
import de.chojo.repbot.web.routes.v1.metrics.util.MetricCache;
import de.chojo.repbot.web.routes.v1.metrics.util.MetricsHolder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

import static de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute.MAX_MONTH;
import static de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute.MAX_MONTH_OFFSET;
import static de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute.MAX_WEEKS;
import static de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute.MAX_WEEK_OFFSET;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Users extends MetricsHolder {
    public Users(Metrics metrics, MetricCache cache) {
        super(cache, metrics);
    }

    @OpenApi(
            summary = "Get the amount of active users per week.",
            operationId = "activeWeek",
            path = "v1/metrics/users/active/week/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Users"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = UsersStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void activeWeek(Context ctx) {
        var stats = metrics().users().week(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equalsIgnoreCase(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Active users per week"));
        }
    }

    @OpenApi(
            summary = "Get the amount of active users per month.",
            operationId = "activeMonth",
            path = "v1/metrics/users/active/month/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Users"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = UsersStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void activeMonth(Context ctx) {
        var stats = metrics().users().month(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Active users per month"));
        }
    }

    @Override
    public void buildRoutes() {
        path("users", () -> path("active", () -> {
            get("week/{offset}/{count}", this::activeWeek);
            get("month/{offset}/{count}", this::activeMonth);
        }));
    }
}
