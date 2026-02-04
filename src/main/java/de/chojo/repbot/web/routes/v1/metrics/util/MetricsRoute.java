/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics.util;

import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.routes.v1.metrics.Commands;
import de.chojo.repbot.web.routes.v1.metrics.Messages;
import de.chojo.repbot.web.routes.v1.metrics.Reputation;
import de.chojo.repbot.web.routes.v1.metrics.Service;
import de.chojo.repbot.web.routes.v1.metrics.Users;

import static io.javalin.apibuilder.ApiBuilder.path;

public class MetricsRoute implements RoutesBuilder {

    public static final int MAX_HOUR_OFFSET = 336;
    public static final int MAX_DAY_OFFSET = 60;
    public static final int MAX_WEEK_OFFSET = 52;
    public static final int MAX_MONTH_OFFSET = 24;
    public static final int MAX_YEAR_OFFSET = 2;

    public static final int MAX_HOURS = 120;
    public static final int MAX_DAYS = 120;
    public static final int MAX_WEEKS = 104;
    public static final int MAX_MONTH = 48;
    private final Reputation reputation;
    private final Commands commands;
    private final Messages messages;
    private final Users users;
    private final MetricCache cache;
    private final Service service;

    public MetricsRoute(de.chojo.repbot.dao.provider.Metrics metrics) {
        cache = new MetricCache();
        reputation = new Reputation(metrics, cache);
        commands = new Commands(metrics, cache);
        messages = new Messages(metrics, cache);
        users = new Users(metrics, cache);
        service = new Service(metrics, cache);
    }

    @Override
    public void buildRoutes() {
        path("metrics", () -> {
            cache.buildRoutes();
            reputation.buildRoutes();
            commands.buildRoutes();
            messages.buildRoutes();
            users.buildRoutes();
            service.buildRoutes();
        });
    }
}
