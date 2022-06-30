package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.web.routes.v1.MetricsHolder;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Users extends MetricsHolder {
    public Users(Metrics metrics) {
        super(metrics);
    }

    public void activeWeek(Context ctx) {
        var stats = metrics().reputation().week(offset(ctx, 1), count(ctx, 24)).join();
        writeImage(ctx, stats.getChart("Active users per week"));
    }

    public void activeMonth(Context ctx) {
        var stats = metrics().reputation().month(offset(ctx, 1), count(ctx, 24)).join();
        writeImage(ctx, stats.getChart("Active users per month"));
    }

    @Override
    public void buildRoutes() {
        path("users", () -> {
            path("active", () -> {
                get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the amount of active users per week.");
                                })
                                .result("200", byte[].class, "image/png")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Week offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously weeks in the chart.")),
                        this::activeWeek));
                get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the amount of active users per month.");
                                })
                                .result("200", byte[].class, "image/png")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Month offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously months in the chart.")),
                        this::activeMonth));
            });
        });
    }
}
