package de.chojo.repbot.web;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.web.erros.ApiError;
import io.javalin.Javalin;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Api {
    private final Javalin javalin;
    private final de.chojo.repbot.web.routes.v1.Metrics metrics;

    public Api(Javalin javalin, Metrics metrics) {
        this.javalin = javalin;
        this.metrics = new de.chojo.repbot.web.routes.v1.Metrics(metrics);
    }

    public void init() {
        javalin.exception(ApiError.class, (err, ctx) -> ctx.result(err.getMessage()).status(err.status()));
        javalin.routes(() -> {
            path("v1", () -> {
                path("metrics", metrics::buildRoutes);
            });
        });
    }
}
