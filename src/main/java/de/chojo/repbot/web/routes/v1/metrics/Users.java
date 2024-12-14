/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.UsersStatistic;
import de.chojo.repbot.web.routes.v1.MetricsHolder;
import de.chojo.repbot.web.routes.v1.MetricsRoute;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;

import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_MONTH;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_MONTH_OFFSET;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_WEEKS;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_WEEK_OFFSET;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

/**
 * Class for handling user metrics routes.
 */
public class Users extends MetricsHolder {
    /**
     * Constructs a new Users instance.
     *
     * @param metrics the metrics provider
     * @param cache the metric cache
     */
    public Users(Metrics metrics, MetricCache cache) {
        super(cache, metrics);
    }

    /**
     * Handles the active week user metrics request.
     *
     * @param ctx the context of the request
     */
    public void activeWeek(Context ctx) {
        var stats = metrics().users().week(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equalsIgnoreCase(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Active users per week"));
        }
    }

    /**
     * Handles the active month user metrics request.
     *
     * @param ctx the context of the request
     */
    public void activeMonth(Context ctx) {
        var stats = metrics().users().month(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Active users per month"));
        }
    }

    /**
     * Builds the routes for user metrics.
     */
    @Override
    public void buildRoutes() {
        path("users", () -> path("active", () -> {
            get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                 .operation(op -> {
                                                                                     op.summary("Get the amount of active users per week.");
                                                                                 })
                                                                                 .result("200", byte[].class, "image/png")
                                                                                 .result("200", UsersStatistic.class, "application/json")
                                                                                 .pathParam("offset", Integer.class, MetricsRoute::offsetWeekDoc)
                                                                                 .pathParam("count", Integer.class, MetricsRoute::countWeekDoc),
                    cache(this::activeWeek)));
            get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                  .operation(op -> {
                                                                                      op.summary("Get the amount of active users per month.");
                                                                                  })
                                                                                  .result("200", byte[].class, "image/png")
                                                                                  .result("200", UsersStatistic.class, "application/json")
                                                                                  .pathParam("offset", Integer.class, MetricsRoute::offsetMonthDoc)
                                                                                  .pathParam("count", Integer.class, MetricsRoute::countMonthDoc),
                    cache(this::activeMonth)));
        }));
    }
}
