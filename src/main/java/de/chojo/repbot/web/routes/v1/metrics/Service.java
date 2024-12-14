/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.LabeledCountStatistic;
import de.chojo.repbot.web.routes.v1.MetricsHolder;
import de.chojo.repbot.web.routes.v1.MetricsRoute;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_DAYS;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_DAY_OFFSET;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_HOURS;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_HOUR_OFFSET;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_MONTH;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_MONTH_OFFSET;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_WEEKS;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_WEEK_OFFSET;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Service extends MetricsHolder {
    public Service(Metrics metrics, MetricCache cache) {
        super(cache, metrics);
    }

    @OpenApi(
            summary = "Get the counts of handled interactions per hour.",
            operationId = "countHour",
            path = "hour/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"User"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LabeledCountStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class),
                    @OpenApiParam(name = "count", type = Integer.class)
            }
    )
    public void countHour(Context ctx) {
        var stats = metrics().service().hour(offset(ctx, MAX_HOUR_OFFSET), count(ctx, MAX_HOURS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Handled interactions per hour"));
        }
    }

    @OpenApi(
            summary = "Get the counts of handled interactions per day.",
            operationId = "countDay",
            path = "day/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"User"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LabeledCountStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class),
                    @OpenApiParam(name = "count", type = Integer.class)
            }
    )
    public void countDay(Context ctx) {
        var stats = metrics().service().day(offset(ctx, MAX_DAY_OFFSET), count(ctx, MAX_DAYS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Handled interactions per day"));
        }
    }

    @OpenApi(
            summary = "Get the counts of handled interactions per week.",
            operationId = "countWeek",
            path = "week/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"User"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LabeledCountStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class),
                    @OpenApiParam(name = "count", type = Integer.class)
            }
    )
    public void countWeek(Context ctx) {
        var stats = metrics().service().week(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Handled interactions per week"));
        }
    }

    @OpenApi(
            summary = "Get the counts of handled interactions per month.",
            operationId = "countMonth",
            path = "month/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"User"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LabeledCountStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class),
                    @OpenApiParam(name = "count", type = Integer.class)
            }
    )
    public void countMonth(Context ctx) {
        var stats = metrics().service().month(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Handled interactions per month"));
        }
    }

    @Override
    public void buildRoutes() {
        path("service", () -> path("count", () -> {
            get("hour/{offset}/{count}", this::countHour);
            get("day/{offset}/{count}", this::countDay);
            get("week/{offset}/{count}", this::countWeek);
            get("month/{offset}/{count}", this::countMonth);
        }));
    }
}
