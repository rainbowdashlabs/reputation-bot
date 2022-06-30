package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.CountsStatistic;
import de.chojo.repbot.dao.snapshots.statistics.DowsStatistic;
import de.chojo.repbot.web.routes.v1.MetricsHolder;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;

import static de.chojo.repbot.web.routes.v1.Metrics.MAX_MONTH;
import static de.chojo.repbot.web.routes.v1.Metrics.MAX_MONTH_OFFSET;
import static de.chojo.repbot.web.routes.v1.Metrics.MAX_WEEKS;
import static de.chojo.repbot.web.routes.v1.Metrics.MAX_WEEK_OFFSET;
import static de.chojo.repbot.web.routes.v1.Metrics.MAX_YEAR_OFFSET;
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
            path("count", () -> {
                get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the counts of given reputation per week.");
                                })
                                .result("200", byte[].class, "image/png")
                                .result("200", CountsStatistic.class, "application/json")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Week offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously weeks in the chart.")),
                        this::countWeek));
                get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the counts of given reputation per month.");
                                })
                                .result("200", byte[].class, "image/png")
                                .result("200", CountsStatistic.class, "application/json")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Month offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously months in the chart.")),
                        this::countMonth));
            });

            path("total", () -> {
                get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the total count of reputation in these weeks.");
                                })
                                .result("200", byte[].class, "image/png")
                                .result("200", CountsStatistic.class, "application/json")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Week offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously weeks in the chart.")),
                        this::totalWeek));
                get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the total count of reputation in these months.");
                                })
                                .result("200", byte[].class, "image/png")
                                .result("200", CountsStatistic.class, "application/json")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Month offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously months in the chart.")),
                        this::totalMonth));
            });

            path("dow", () -> {
                get("week/{offset}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get reputation per day of week.");
                                })
                                .result("200", byte[].class, "image/png")
                                .result("200", DowsStatistic.class, "application/json")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Week offset. 0 is current.")),
                        this::dowWeek));
                get("month/{offset}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get average reputation per day of week in a month.");
                                })
                                .result("200", byte[].class, "image/png")
                                .result("200", DowsStatistic.class, "application/json")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Month offset. 0 is current.")),
                        this::dowMonth));
                get("year/{offset}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get average reputation per day of week in a year.");
                                })
                                .result("200", byte[].class, "image/png")
                                .result("200", DowsStatistic.class, "application/json")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Year offset. 0 is current.")),
                        this::dowYear));
            });
        });
    }
}
