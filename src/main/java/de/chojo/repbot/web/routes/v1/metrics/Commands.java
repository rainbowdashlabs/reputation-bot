/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.CommandsStatistic;
import de.chojo.repbot.util.Text;
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

public class Commands extends MetricsHolder {
    public Commands(Metrics metrics, MetricCache cache) {
        super(cache, metrics);
    }

    @OpenApi(
            summary = "Get command usages for a week.",
            operationId = "usageWeek",
            path = "v1/metrics/commands/usage/week/{offset}",
            methods = HttpMethod.GET,
            tags = {"Commands"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CommandsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true)
            }
    )
    public void usageWeek(Context ctx) {
        var stats = metrics().commands().week(offset(ctx, MAX_WEEK_OFFSET));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Command usage for week " + Text.date(stats.date())));
        }
    }

    @OpenApi(
            summary = "Get command usages for a month.",
            operationId = "usageMonth",
            path = "v1/metrics/commands/usage/month/{offset}",
            methods = HttpMethod.GET,
            tags = {"Commands"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CommandsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true)
            }
    )
    public void usageMonth(Context ctx) {
        var stats = metrics().commands().month(offset(ctx, MAX_MONTH_OFFSET));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Command usage for month " + Text.month(stats.date())));
        }
    }

    @OpenApi(
            summary = "Get the amount of executed commands per week.",
            operationId = "countWeek",
            path = "v1/metrics/commands/count/week/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Commands"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CommandsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void countWeek(Context ctx) {
        var stats = metrics().commands().week(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Commands executed per week"));
        }
    }

    @OpenApi(
            summary = "Get the amount of executed commands per month.",
            operationId = "countMonth",
            path = "v1/metrics/commands/count/month/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Commands"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CommandsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void countMonth(Context ctx) {
        var stats = metrics().commands().month(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Commands executed per month"));
        }
    }

    @Override
    public void buildRoutes() {
        path("commands", () -> {
            path("count", () -> {
                get("week/{offset}/{count}", this::countWeek);
                get("month/{offset}/{count}", this::countMonth);
            });

            path("usage", () -> {
                get("week/{offset}", this::usageWeek);
                get("month/{offset}", this::usageMonth);
            });
        });
    }
}
