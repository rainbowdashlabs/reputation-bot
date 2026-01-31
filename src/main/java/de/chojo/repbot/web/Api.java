/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.web.routes.v1.metrics.util.MetricsRoute;
import de.chojo.repbot.web.routes.v1.session.SessionRoute;
import de.chojo.repbot.web.routes.v1.settings.SettingsRoute;
import de.chojo.repbot.web.sessions.SessionService;
import io.javalin.http.ContentType;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.after;
import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.path;
import static org.slf4j.LoggerFactory.getLogger;

public class Api {
    private static final Logger log = getLogger(Api.class);
    private final SessionService sessionService;
    private final MetricsRoute metricsRoute;
    private final SessionRoute sessionRoute;
    private final SettingsRoute settingsRoute;

    public Api(SessionService sessionService, Metrics metrics) {
        this.sessionService = sessionService;
        metricsRoute = new MetricsRoute(metrics);
        sessionRoute = new SessionRoute(sessionService);
        settingsRoute = new SettingsRoute();
    }

    public void init() {
        before(ctx -> log.trace("Received request on route: {} {}\nHeaders:\n{}\nBody:\n{}",
                ctx.method() + " " + ctx.url(),
                ctx.queryString(),
                ctx.headerMap().entrySet().stream().map(h -> "   " + h.getKey() + ": " + h.getValue())
                   .collect(Collectors.joining("\n")),
                ctx.body().substring(0, Math.min(ctx.body().length(), 180))));
        after(ctx -> {
            log.trace("Answered request on route: {} {}\nStatus: {}\nHeaders:\n{}\nBody:\n{}",
                    ctx.method() + " " + ctx.url(),
                    ctx.queryString(),
                    ctx.status(),
                    ctx.res().getHeaderNames().stream().map(h -> "   " + h + ": " + ctx.res().getHeader(h))
                       .collect(Collectors.joining("\n")),
                    ContentType.OCTET_STREAM.equals(ctx.contentType()) ? "Bytes"
                            : Objects.requireNonNullElse(ctx.result(), "")
                                     .substring(0, Math.min(
                                             Objects.requireNonNullElse(ctx.result(), "").length(), 180)));
        });
        path("v1", () -> {
            metricsRoute.buildRoutes();
            sessionRoute.buildRoutes();
            settingsRoute.buildRoutes();
        });
    }
}
