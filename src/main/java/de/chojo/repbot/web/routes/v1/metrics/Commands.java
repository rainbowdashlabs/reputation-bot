package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.CommandsStatistic;
import de.chojo.repbot.util.TimeFormatter;
import de.chojo.repbot.web.routes.v1.MetricsHolder;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;

import static de.chojo.repbot.web.routes.v1.Metrics.MAX_MONTH;
import static de.chojo.repbot.web.routes.v1.Metrics.MAX_MONTH_OFFSET;
import static de.chojo.repbot.web.routes.v1.Metrics.MAX_WEEKS;
import static de.chojo.repbot.web.routes.v1.Metrics.MAX_WEEK_OFFSET;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Commands extends MetricsHolder {
    public Commands(Metrics metrics) {
        super(metrics);
    }

    public void usageWeek(Context ctx) {
        var stats = metrics().commands().week(offset(ctx, MAX_WEEK_OFFSET)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Command usage for week " + TimeFormatter.date(stats.date())));
        }
    }

    public void usageMonth(Context ctx) {
        var stats = metrics().commands().month(offset(ctx, MAX_MONTH_OFFSET)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Command usage for month " + TimeFormatter.month(stats.date())));
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
        var stats = metrics().commands().week(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH)).join();
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
                                .pathParam("offset", Integer.class, p -> p.setDescription("Week offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously weeks in the chart.")),
                        this::countWeek));
                get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the amount of exectued commands per month.");
                                })
                                .result("200", byte[].class, "image/png")
                                .result("200", CommandsStatistic.class, "application/json")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Month offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously months in the chart.")),
                        this::countMonth));
            });

            path("usage", () -> {
                get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get command usages for a week.");
                                })
                                .result("200", byte[].class, "image/png")
                                .result("200", CommandsStatistic.class, "application/json")
                                .pathParam("offset", Integer.class, p -> {
                                    p.setDescription("Week offset. 0 is current.");
                                }),
                        this::usageWeek));
                get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get command usages for a month.");
                                })
                                .result("200", byte[].class, "image/png")
                                .result("200", CommandsStatistic.class, "application/json")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Month offset. 0 is current.")),
                        this::usageMonth));
            });
        });
    }
}
