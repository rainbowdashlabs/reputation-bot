package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.CountsStatistic;
import de.chojo.repbot.dao.snapshots.statistics.DowsStatistic;
import de.chojo.repbot.dao.snapshots.statistics.LabeledCountStatistic;
import de.chojo.repbot.web.routes.v1.MetricsHolder;
import de.chojo.repbot.web.routes.v1.MetricsRoute;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;

import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_MONTH;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_MONTH_OFFSET;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_WEEKS;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_WEEK_OFFSET;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_YEAR_OFFSET;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Reputation extends MetricsHolder {
    public Reputation(Metrics metrics, MetricCache cache) {
        super(cache, metrics);
    }

    /*
    This function is broken, but I don't know how to fix it.
     */
    public void countWeek(Context ctx) {
        var stats = metrics().reputation().week(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation per week"));
        }
    }

    public void countMonth(Context ctx) {
        var stats = metrics().reputation().month(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation per month"));
        }
    }

    public void countTypeWeek(Context ctx) {
        var stats = metrics().reputation().typeWeek(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation per week by type"));
        }
    }

    public void countTypeMonth(Context ctx) {
        var stats = metrics().reputation().typeMonth(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation per month by type"));
        }
    }

    public void totalWeek(Context ctx) {
        var stats = metrics().reputation().totalWeek(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total reputation per week"));
        }
    }

    public void totalMonth(Context ctx) {
        var stats = metrics().reputation().totalMonth(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total reputation per month"));
        }
    }

    public void totalTypeWeek(Context ctx) {
        var stats = metrics().reputation().typeTotalWeek(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total reputation per week by type"));
        }
    }

    public void totalTypeMonth(Context ctx) {
        var stats = metrics().reputation().typeTotalMonth(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total reputation per month by type"));
        }
    }

    public void dowWeek(Context ctx) {
        var stats = metrics().reputation().dowWeek(offset(ctx, MAX_WEEK_OFFSET)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation given per day of week average"));
        }
    }

    public void dowMonth(Context ctx) {
        var stats = metrics().reputation().dowMonth(offset(ctx, MAX_MONTH_OFFSET)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation given per day of week average"));
        }
    }

    public void dowYear(Context ctx) {
        var stats = metrics().reputation().dowYear(offset(ctx, MAX_YEAR_OFFSET)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation given per day of week average"));
        }
    }

    @Override
    public void buildRoutes() {
        path("reputation", () -> {
            path("type", () -> {
                path("count", () -> {
                    get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                         .operation(op -> {
                                                                                             op.summary("Get the counts of given reputation per week split into type.");
                                                                                         })
                                                                                         .result("200", byte[].class, "image/png")
                                                                                         .result("200", LabeledCountStatistic.class, "application/json")
                                                                                         .pathParam("offset", Integer.class, MetricsRoute::offsetWeekDoc)
                                                                                         .pathParam("count", Integer.class, MetricsRoute::countWeekDoc),
                            cache(this::countTypeWeek)));
                    get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                          .operation(op -> {
                                                                                              op.summary("Get the counts of given reputation per month split into type.");
                                                                                          })
                                                                                          .result("200", byte[].class, "image/png")
                                                                                          .result("200", LabeledCountStatistic.class, "application/json")
                                                                                          .pathParam("offset", Integer.class, MetricsRoute::offsetMonthDoc)
                                                                                          .pathParam("count", Integer.class, MetricsRoute::countMonthDoc),
                            cache(this::countTypeMonth)));
                });

                path("total", () -> {
                    get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                         .operation(op -> {
                                                                                             op.summary("Get the total count of reputation in these weeks split into type.");
                                                                                         })
                                                                                         .result("200", byte[].class, "image/png")
                                                                                         .result("200", LabeledCountStatistic.class, "application/json")
                                                                                         .pathParam("offset", Integer.class, MetricsRoute::offsetWeekDoc)
                                                                                         .pathParam("count", Integer.class, MetricsRoute::countWeekDoc),
                            cache(this::totalTypeWeek)));
                    get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                          .operation(op -> {
                                                                                              op.summary("Get the total count of reputation in these months split into type.");
                                                                                          })
                                                                                          .result("200", byte[].class, "image/png")
                                                                                          .result("200", LabeledCountStatistic.class, "application/json")
                                                                                          .pathParam("offset", Integer.class, MetricsRoute::offsetMonthDoc)
                                                                                          .pathParam("count", Integer.class, MetricsRoute::countMonthDoc),
                            cache(this::totalTypeMonth)));
                });
            });
            path("count", () -> {
                get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                     .operation(op -> {
                                                                                         op.summary("Get the counts of given reputation per week.");
                                                                                     })
                                                                                     .result("200", byte[].class, "image/png")
                                                                                     .result("200", CountsStatistic.class, "application/json")
                                                                                     .pathParam("offset", Integer.class, MetricsRoute::offsetWeekDoc)
                                                                                     .pathParam("count", Integer.class, MetricsRoute::countWeekDoc),
                        cache(this::countWeek)));
                get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                      .operation(op -> {
                                                                                          op.summary("Get the counts of given reputation per month.");
                                                                                      })
                                                                                      .result("200", byte[].class, "image/png")
                                                                                      .result("200", CountsStatistic.class, "application/json")
                                                                                      .pathParam("offset", Integer.class, MetricsRoute::offsetMonthDoc)
                                                                                      .pathParam("count", Integer.class, MetricsRoute::countMonthDoc),
                        cache(this::countMonth)));
            });

            path("total", () -> {
                get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                     .operation(op -> {
                                                                                         op.summary("Get the total count of reputation in these weeks.");
                                                                                     })
                                                                                     .result("200", byte[].class, "image/png")
                                                                                     .result("200", CountsStatistic.class, "application/json")
                                                                                     .pathParam("offset", Integer.class, MetricsRoute::offsetWeekDoc)
                                                                                     .pathParam("count", Integer.class, MetricsRoute::countWeekDoc),
                        cache(this::totalWeek)));
                get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                                      .operation(op -> {
                                                                                          op.summary("Get the total count of reputation in these months.");
                                                                                      })
                                                                                      .result("200", byte[].class, "image/png")
                                                                                      .result("200", CountsStatistic.class, "application/json")
                                                                                      .pathParam("offset", Integer.class, MetricsRoute::offsetMonthDoc)
                                                                                      .pathParam("count", Integer.class, MetricsRoute::countMonthDoc),
                        cache(this::totalMonth)));
            });

            path("dow", () -> {
                get("week/{offset}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                             .operation(op -> {
                                                                                 op.summary("Get reputation per day of week.");
                                                                             })
                                                                             .result("200", byte[].class, "image/png")
                                                                             .result("200", DowsStatistic.class, "application/json")
                                                                             .pathParam("offset", Integer.class, MetricsRoute::offsetWeekDoc),
                        cache(this::dowWeek)));
                get("month/{offset}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                              .operation(op -> {
                                                                                  op.summary("Get average reputation per day of week in a month.");
                                                                              })
                                                                              .result("200", byte[].class, "image/png")
                                                                              .result("200", DowsStatistic.class, "application/json")
                                                                              .pathParam("offset", Integer.class, MetricsRoute::offsetMonthDoc),
                        cache(this::dowMonth)));
                get("year/{offset}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                                                             .operation(op -> {
                                                                                 op.summary("Get average reputation per day of week in a year.");
                                                                             })
                                                                             .result("200", byte[].class, "image/png")
                                                                             .result("200", DowsStatistic.class, "application/json")
                                                                             .pathParam("offset", Integer.class, MetricsRoute::offsetYearDoc),
                        cache(this::dowYear)));
            });
        });
    }
}
