package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.web.routes.v1.MetricsHolder;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Messages extends MetricsHolder {
    public Messages(Metrics metrics) {
        super(metrics);
    }

    public void countDay(Context ctx) {
        var stats = metrics().messages().day(offset(ctx, 1), count(ctx, 24)).join();
        writeImage(ctx, stats.getChart("Reputation per day"));
    }

    public void countWeek(Context ctx) {
        var stats = metrics().messages().week(offset(ctx, 1), count(ctx, 24)).join();
        writeImage(ctx, stats.getChart("Reputation per week"));
    }

    public void countMonth(Context ctx) {
        var stats = metrics().messages().month(offset(ctx, 1), count(ctx, 24)).join();
        writeImage(ctx, stats.getChart("Reputation per month"));
    }

    public void totalDay(Context ctx) {
        var stats = metrics().messages().totalWeek(offset(ctx, 1), count(ctx, 24)).join();
        writeImage(ctx, stats.getChart("Total reputation per day"));
    }

    public void totalWeek(Context ctx) {
        var stats = metrics().messages().totalWeek(offset(ctx, 1), count(ctx, 24)).join();
        writeImage(ctx, stats.getChart("Total reputation per week"));
    }

    public void totalMonth(Context ctx) {
        var stats = metrics().messages().totalMonth(offset(ctx, 1), count(ctx, 24)).join();
        writeImage(ctx, stats.getChart("Total reputation per month"));
    }

    @Override
    public void buildRoutes() {
        path("messages", () -> {
            path("count", () -> {
                get("day/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the counts of analyzed messages per day.");
                                })
                                .result("200", byte[].class, "image/png")
                                .pathParam("offset", Integer.class, p -> p.setDescription("day offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously days in the chart.")),
                        this::countDay));
                get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the counts of analyzed messages per week.");
                                })
                                .result("200", byte[].class, "image/png")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Week offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously weeks in the chart.")),
                        this::countWeek));
                get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the counts of analyzed messages per month.");
                                })
                                .result("200", byte[].class, "image/png")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Month offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously months in the chart.")),
                        this::countMonth));
            });

            path("total", () -> {
                get("day/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the total count of analyzed messages in these days.");
                                })
                                .result("200", byte[].class, "image/png")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Day offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously days in the chart.")),
                        this::totalDay));
                get("week/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the total count of analyzed messages in these weeks.");
                                })
                                .result("200", byte[].class, "image/png")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Week offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously weeks in the chart.")),
                        this::totalWeek));
                get("month/{offset}/{count}", OpenApiBuilder.documented(OpenApiBuilder.document()
                                .operation(op -> {
                                    op.summary("Get the total count of analyzed messages in these months.");
                                })
                                .result("200", byte[].class, "image/png")
                                .pathParam("offset", Integer.class, p -> p.setDescription("Month offset. 0 is current."))
                                .pathParam("count", Integer.class, p -> p.setDescription("Amount of previously months in the chart.")),
                        this::totalMonth));
            });
        });
    }
}
