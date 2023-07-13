/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.CommandsStatistic;
import de.chojo.repbot.util.Text;
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

public class Commands extends MetricsHolder {
    public Commands(Metrics metrics, MetricCache cache) {
        super(cache, metrics);
    }

    public void usageWeek(Context ctx) {
        var stats = metrics().commands().week(offset(ctx, MAX_WEEK_OFFSET)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Command usage for week " + Text.date(stats.date())));
        }
    }

    public void usageMonth(Context ctx) {
        var stats = metrics().commands().month(offset(ctx, MAX_MONTH_OFFSET)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Command usage for month " + Text.month(stats.date())));
        }
    }

    public void countWeek(Context ctx) {
        var stats = metrics().commands().week(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Commands executed per week"));
        }
    }

    public void countMonth(Context ctx) {
        var stats = metrics().commands().month(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH)).join();
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
                get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                     .operation(op -> {
                                                                                         op.summary("Get the amount of exectued commands per week.");
                                                                                     })
                                                                                     .result("200", byte[].class, "image/png")
                                                                                     .result("200", CommandsStatistic.class, "application/json")
                                                                                     .pathParam("offset", Integer.class, MetricsRoute::offsetWeekDoc)
                                                                                     .pathParam("count", Integer.class, MetricsRoute::countWeekDoc),
                        cache(this::countWeek)));
                get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                      .operation(op -> {
                                                                                          op.summary("Get the amount of exectued commands per month.");
                                                                                      })
                                                                                      .result("200", byte[].class, "image/png")
                                                                                      .result("200", CommandsStatistic.class, "application/json")
                                                                                      .pathParam("offset", Integer.class, MetricsRoute::offsetMonthDoc)
                                                                                      .pathParam("count", Integer.class, MetricsRoute::countMonthDoc),
                        cache(this::countMonth)));
            });

            path("usage", () -> {
                get("week/{offset}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                             .operation(op -> {
                                                                                 op.summary("Get command usages for a week.");
                                                                             })
                                                                             .result("200", byte[].class, "image/png")
                                                                             .result("200", CommandsStatistic.class, "application/json")
                                                                             .pathParam("offset", Integer.class, MetricsRoute::offsetWeekDoc),
                        cache(this::usageWeek)));
                get("month/{offset}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                              .operation(op -> {
                                                                                  op.summary("Get command usages for a month.");
                                                                              })
                                                                              .result("200", byte[].class, "image/png")
                                                                              .result("200", CommandsStatistic.class, "application/json")
                                                                              .pathParam("offset", Integer.class, MetricsRoute::offsetMonthDoc),
                        cache(this::usageMonth)));
            });
        });
    }
}
