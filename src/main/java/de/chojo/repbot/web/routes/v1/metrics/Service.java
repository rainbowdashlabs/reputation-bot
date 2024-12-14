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
 * Service class for handling metrics related to interactions.
 */
public class Service extends MetricsHolder {
    /**
     * Constructs a new Service.
     *
     * @param metrics the metrics provider
     * @param cache the metric cache
     */
    public Service(Metrics metrics, MetricCache cache) {
        super(cache, metrics);
    }

    /**
     * Handles the request to get the count of handled interactions per hour.
     *
     * @param ctx the context of the request
     */
    public void countHour(Context ctx) {
        var stats = metrics().service().hour(offset(ctx, MAX_HOUR_OFFSET), count(ctx, MAX_HOURS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Handled interactions per hour"));
        }
    }

    /**
     * Handles the request to get the count of handled interactions per day.
     *
     * @param ctx the context of the request
     */
    public void countDay(Context ctx) {
        var stats = metrics().service().day(offset(ctx, MAX_DAY_OFFSET), count(ctx, MAX_DAYS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Handled interactions per day"));
        }
    }

    /**
     * Handles the request to get the count of handled interactions per week.
     *
     * @param ctx the context of the request
     */
    public void countWeek(Context ctx) {
        var stats = metrics().service().week(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Handled interactions per week"));
        }
    }

    /**
     * Handles the request to get the count of handled interactions per month.
     *
     * @param ctx the context of the request
     */
    public void countMonth(Context ctx) {
        var stats = metrics().service().month(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Handled interactions per month"));
        }
    }

    /**
     * Builds the routes for the service metrics.
     */
    @Override
    public void buildRoutes() {
        path("service", () -> path("count", () -> {
            get("hour/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                 .operation(op -> {
                                                                                     op.summary("Get the counts of handled interactions per hour.");
                                                                                 })
                                                                                 .result("200", byte[].class, "image/png")
                                                                                 .result("200", LabeledCountStatistic.class, "application/json")
                                                                                 .pathParam("offset", Integer.class, MetricsRoute::offsetHourDoc)
                                                                                 .pathParam("count", Integer.class, MetricsRoute::countHourDoc),
                    cache(this::countHour)));
            get("day/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                .operation(op -> {
                                                                                    op.summary("Get the counts of handled interactions per day.");
                                                                                })
                                                                                .result("200", byte[].class, "image/png")
                                                                                .result("200", LabeledCountStatistic.class, "application/json")
                                                                                .pathParam("offset", Integer.class, MetricsRoute::offsetDayDoc)
                                                                                .pathParam("count", Integer.class, MetricsRoute::countDayDoc),
                    cache(this::countDay)));
            get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                 .operation(op -> {
                                                                                     op.summary("Get the counts of handled interactions per week.");
                                                                                 })
                                                                                 .result("200", byte[].class, "image/png")
                                                                                 .result("200", LabeledCountStatistic.class, "application/json")
                                                                                 .pathParam("offset", Integer.class, MetricsRoute::offsetWeekDoc)
                                                                                 .pathParam("count", Integer.class, MetricsRoute::countWeekDoc),
                    cache(this::countWeek)));
            get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                  .operation(op -> {
                                                                                      op.summary("Get the counts of handled interactions per month.");
                                                                                  })
                                                                                  .result("200", byte[].class, "image/png")
                                                                                  .result("200", LabeledCountStatistic.class, "application/json")
                                                                                  .pathParam("offset", Integer.class, MetricsRoute::offsetMonthDoc)
                                                                                  .pathParam("count", Integer.class, MetricsRoute::countMonthDoc),
                    cache(this::countMonth)));
        }));
    }
}
