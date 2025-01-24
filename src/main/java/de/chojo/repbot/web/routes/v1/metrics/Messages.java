/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.CountsStatistic;
import de.chojo.repbot.web.routes.v1.MetricsHolder;
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

/**
 * Handles the metrics related to messages.
 */
public class Messages extends MetricsHolder {
    /**
     * Constructs a new Messages handler.
     *
     * @param metrics the Metrics provider
     * @param cache the MetricCache
     */
    public Messages(Metrics metrics, MetricCache cache) {
        super(cache, metrics);
    }

    /**
     * Handles the request to get the count of messages analyzed per hour.
     *
     * @param ctx the Javalin context
     */
    @OpenApi(
            summary = "Get the counts of analyzed messages per hour.",
            operationId = "countHour",
            path = "messages/hour/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void countHour(Context ctx) {
        var stats = metrics().messages().hour(offset(ctx, MAX_HOUR_OFFSET), count(ctx, MAX_HOURS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Messages analyzed per hour"));
        }
    }

    /**
     * Handles the request to get the count of messages analyzed per day.
     *
     * @param ctx the Javalin context
     */
    @OpenApi(
            summary = "Get the counts of analyzed messages per day.",
            operationId = "countDay",
            path = "messages/day/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void countDay(Context ctx) {
        var stats = metrics().messages().day(offset(ctx, MAX_DAY_OFFSET), count(ctx, MAX_DAYS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Messages analyzed per day"));
        }
    }

    /**
     * Handles the request to get the count of messages analyzed per week.
     *
     * @param ctx the Javalin context
     */
    @OpenApi(
            summary = "Get the counts of analyzed messages per week.",
            operationId = "countWeek",
            path = "messages/week/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void countWeek(Context ctx) {
        var stats = metrics().messages().week(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Messages analyzed per week"));
        }
    }

    /**
     * Handles the request to get the count of messages analyzed per month.
     *
     * @param ctx the Javalin context
     */
    @OpenApi(
            summary = "Get the counts of analyzed messages per month.",
            operationId = "countMonth",
            path = "messages/month/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void countMonth(Context ctx) {
        var stats = metrics().messages().month(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Messages analyzed per month"));
        }
    }

    /**
     * Handles the request to get the total count of messages analyzed per day.
     *
     * @param ctx the Javalin context
     */
    @OpenApi(
            summary = "Get the total count of analyzed messages in these days.",
            operationId = "totalDay",
            path = "messages/day/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void totalDay(Context ctx) {
        var stats = metrics().messages().totalDay(offset(ctx, MAX_DAY_OFFSET), count(ctx, MAX_DAYS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total messages analyzed"));
        }
    }

    /**
     * Handles the request to get the total count of messages analyzed per week.
     *
     * @param ctx the Javalin context
     */
    @OpenApi(
            summary = "Get the total count of analyzed messages in these weeks.",
            operationId = "totalWeek",
            path = "messages/week/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void totalWeek(Context ctx) {
        var stats = metrics().messages().totalWeek(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total messages analyzed"));
        }
    }

    /**
     * Handles the request to get the total count of messages analyzed per month.
     *
     * @param ctx the Javalin context
     */
    @OpenApi(
            summary = "Get the total count of analyzed messages in these months.",
            operationId = "totalMonth",
            path = "messages/month/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Messages"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void totalMonth(Context ctx) {
        var stats = metrics().messages().totalMonth(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total messages analyzed"));
        }
    }

    /**
     * Builds the routes for message metrics.
     */
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
