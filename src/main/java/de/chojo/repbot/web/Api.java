package de.chojo.repbot.web;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.web.erros.ApiError;
import io.javalin.Javalin;
import io.javalin.http.util.RateLimiter;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.path;
import static org.slf4j.LoggerFactory.getLogger;

public class Api {
    private static final Logger log = getLogger(Api.class);
    private final Javalin javalin;
    private final de.chojo.repbot.web.routes.v1.Metrics metrics;

    public Api(Javalin javalin, Metrics metrics) {
        this.javalin = javalin;
        this.metrics = new de.chojo.repbot.web.routes.v1.Metrics(metrics);
    }

    public void init() {
        javalin.exception(ApiError.class, (err, ctx) -> ctx.result(err.getMessage()).status(err.status()));
        javalin.routes(() -> {
            before(ctx -> {
                log.debug("Received request on {}.", ctx.path());

            });

            path("v1", () -> {
                path("metrics", metrics::buildRoutes);
            });
        });
    }
}
