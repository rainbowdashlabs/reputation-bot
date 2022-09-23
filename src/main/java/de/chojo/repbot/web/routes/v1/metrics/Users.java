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

public class Users extends MetricsHolder {
    public Users(Metrics metrics, MetricCache cache) {
        super(cache, metrics);
    }

    public void activeWeek(Context ctx) {
        var stats = metrics().users().week(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS)).join();
        if ("application/json".equalsIgnoreCase(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Active users per week"));
        }
    }

    public void activeMonth(Context ctx) {
        var stats = metrics().users().month(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH)).join();
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Active users per month"));
        }
    }

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
