/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.CountsStatistic;
import de.chojo.repbot.web.routes.v1.MetricsHolder;
import de.chojo.repbot.web.routes.v1.MetricsRoute;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;

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
                get("hour/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                     .operation(op -> {
                                                                                         op.summary("Get the counts of analyzed messages per hour.");
                                                                                     })
                                                                                     .result("200", byte[].class, "image/png")
                                                                                     .result("200", CountsStatistic.class, "application/json")
                                                                                     .pathParam("offset", Integer.class, MetricsRoute::offsetHourDoc)
                                                                                     .pathParam("count", Integer.class, MetricsRoute::countHourDoc),
                        cache(this::countHour)));
                get("day/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                    .operation(op -> {
                                                                                        op.summary("Get the counts of analyzed messages per day.");
                                                                                    })
                                                                                    .result("200", byte[].class, "image/png")
                                                                                    .result("200", CountsStatistic.class, "application/json")
                                                                                    .pathParam("offset", Integer.class, MetricsRoute::offsetDayDoc)
                                                                                    .pathParam("count", Integer.class, MetricsRoute::countDayDoc),
                        cache(this::countDay)));
                get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                     .operation(op -> {
                                                                                         op.summary("Get the counts of analyzed messages per week.");
                                                                                     })
                                                                                     .result("200", byte[].class, "image/png")
                                                                                     .result("200", CountsStatistic.class, "application/json")
                                                                                     .pathParam("offset", Integer.class, MetricsRoute::offsetWeekDoc)
                                                                                     .pathParam("count", Integer.class, MetricsRoute::countWeekDoc),
                        cache(this::countWeek)));
                get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                      .operation(op -> {
                                                                                          op.summary("Get the counts of analyzed messages per month.");
                                                                                      })
                                                                                      .result("200", byte[].class, "image/png")
                                                                                      .result("200", CountsStatistic.class, "application/json")
                                                                                      .pathParam("offset", Integer.class, MetricsRoute::offsetMonthDoc)
                                                                                      .pathParam("count", Integer.class, MetricsRoute::countMonthDoc),
                        cache(this::countMonth)));
            });

            path("total", () -> {
                get("day/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                    .operation(op -> {
                                                                                        op.summary("Get the total count of analyzed messages in these days.");
                                                                                    })
                                                                                    .result("200", byte[].class, "image/png")
                                                                                    .result("200", CountsStatistic.class, "application/json")
                                                                                    .pathParam("offset", Integer.class, MetricsRoute::offsetDayDoc)
                                                                                    .pathParam("count", Integer.class, MetricsRoute::countDayDoc),
                        cache(this::totalDay)));
                get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                     .operation(op -> {
                                                                                         op.summary("Get the total count of analyzed messages in these weeks.");
                                                                                     })
                                                                                     .result("200", byte[].class, "image/png")
                                                                                     .result("200", CountsStatistic.class, "application/json")
                                                                                     .pathParam("offset", Integer.class, MetricsRoute::offsetWeekDoc)
                                                                                     .pathParam("count", Integer.class, MetricsRoute::countWeekDoc),
                        cache(this::totalWeek)));
                get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                      .operation(op -> {
                                                                                          op.summary("Get the total count of analyzed messages in these months.");
                                                                                      })
                                                                                      .result("200", byte[].class, "image/png")
                                                                                      .result("200", CountsStatistic.class, "application/json")
                                                                                      .pathParam("offset", Integer.class, MetricsRoute::offsetMonthDoc)
                                                                                      .pathParam("count", Integer.class, MetricsRoute::countMonthDoc),
                        cache(this::totalMonth)));
            });
        });
    }
}
