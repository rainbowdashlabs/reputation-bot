/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.CountsStatistic;
import de.chojo.repbot.web.routes.v1.metrics.util.MetricCache;
import de.chojo.repbot.web.routes.v1.metrics.util.MetricsHolder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

import static de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute.MAX_DAYS;
import static de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute.MAX_DAY_OFFSET;
import static de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute.MAX_HOURS;
import static de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute.MAX_HOUR_OFFSET;
import static de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute.MAX_MONTH;
import static de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute.MAX_MONTH_OFFSET;
import static de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute.MAX_WEEKS;
import static de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute.MAX_WEEK_OFFSET;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Messages extends MetricsHolder {
    public Messages(Metrics metrics, MetricCache cache) {
        super(cache, metrics);
    }

    @OpenApi(
            summary = "Get the counts of analyzed messages per hour.",
            operationId = "countHour",
            path = "v1/metrics/messages/hour/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                @OpenApiParam(name = "offset", type = Integer.class, required = true),
                @OpenApiParam(name = "count", type = Integer.class, required = true)
            })
    public void countHour(Context ctx) {
        var stats = metrics().messages().hour(offset(ctx, MAX_HOUR_OFFSET), count(ctx, MAX_HOURS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Messages analyzed per hour"));
        }
    }

    @OpenApi(
            summary = "Get the counts of analyzed messages per day.",
            operationId = "countDay",
            path = "v1/metrics/messages/day/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                @OpenApiParam(name = "offset", type = Integer.class, required = true),
                @OpenApiParam(name = "count", type = Integer.class, required = true)
            })
    public void countDay(Context ctx) {
        var stats = metrics().messages().day(offset(ctx, MAX_DAY_OFFSET), count(ctx, MAX_DAYS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Messages analyzed per day"));
        }
    }

    @OpenApi(
            summary = "Get the counts of analyzed messages per week.",
            operationId = "countWeek",
            path = "v1/metrics/messages/week/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                @OpenApiParam(name = "offset", type = Integer.class, required = true),
                @OpenApiParam(name = "count", type = Integer.class, required = true)
            })
    public void countWeek(Context ctx) {
        var stats = metrics().messages().week(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Messages analyzed per week"));
        }
    }

    @OpenApi(
            summary = "Get the counts of analyzed messages per month.",
            operationId = "countMonth",
            path = "v1/metrics/messages/month/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                @OpenApiParam(name = "offset", type = Integer.class, required = true),
                @OpenApiParam(name = "count", type = Integer.class, required = true)
            })
    public void countMonth(Context ctx) {
        var stats = metrics().messages().month(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Messages analyzed per month"));
        }
    }

    @OpenApi(
            summary = "Get the total count of analyzed messages in these days.",
            operationId = "totalDay",
            path = "v1/metrics/messages/day/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                @OpenApiParam(name = "offset", type = Integer.class, required = true),
                @OpenApiParam(name = "count", type = Integer.class, required = true)
            })
    public void totalDay(Context ctx) {
        var stats = metrics().messages().totalDay(offset(ctx, MAX_DAY_OFFSET), count(ctx, MAX_DAYS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total messages analyzed"));
        }
    }

    @OpenApi(
            summary = "Get the total count of analyzed messages in these weeks.",
            operationId = "totalWeek",
            path = "v1/metrics/messages/week/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                @OpenApiParam(name = "offset", type = Integer.class, required = true),
                @OpenApiParam(name = "count", type = Integer.class, required = true)
            })
    public void totalWeek(Context ctx) {
        var stats = metrics().messages().totalWeek(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total messages analyzed"));
        }
    }

    @OpenApi(
            summary = "Get the total count of analyzed messages in these months.",
            operationId = "totalMonth",
            path = "v1/metrics/messages/month/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                @OpenApiParam(name = "offset", type = Integer.class, required = true),
                @OpenApiParam(name = "count", type = Integer.class, required = true)
            })
    public void totalMonth(Context ctx) {
        var stats = metrics().messages().totalMonth(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total messages analyzed"));
        }
    }

    @Override
    public void buildRoutes() {
        path("messages", () -> {
            path("count", () -> {
                get("hour/{offset}/{count}", this::countHour);
                get("day/{offset}/{count}", this::countDay);
                get("week/{offset}/{count}", this::countWeek);
                get("month/{offset}/{count}", this::countMonth);
            });

            path("total", () -> {
                get("day/{offset}/{count}", this::totalDay);
                get("week/{offset}/{count}", this::totalWeek);
                get("month/{offset}/{count}", this::totalMonth);
            });
        });
    }
}
