/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1;

import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.routes.v1.metrics.Commands;
import de.chojo.repbot.web.routes.v1.metrics.Messages;
import de.chojo.repbot.web.routes.v1.metrics.MetricCache;
import de.chojo.repbot.web.routes.v1.metrics.Reputation;
import de.chojo.repbot.web.routes.v1.metrics.Service;
import de.chojo.repbot.web.routes.v1.metrics.Users;
import io.javalin.router.matcher.PathSegment;
import io.javalin.router.matcher.PathSegment.Parameter;

/**
 * Class for building and managing metric-related routes.
 */
public class MetricsRoute implements RoutesBuilder {

    /**
     * Maximum offset for hours.
     */
    public static final int MAX_HOUR_OFFSET = 336;

    /**
     * Maximum offset for days.
     */
    public static final int MAX_DAY_OFFSET = 60;

    /**
     * Maximum offset for weeks.
     */
    public static final int MAX_WEEK_OFFSET = 52;

    /**
     * Maximum offset for months.
     */
    public static final int MAX_MONTH_OFFSET = 24;

    /**
     * Maximum offset for years.
     */
    public static final int MAX_YEAR_OFFSET = 2;

    /**
     * Maximum count for hours.
     */
    public static final int MAX_HOURS = 120;

    /**
     * Maximum count for days.
     */
    public static final int MAX_DAYS = 120;

    /**
     * Maximum count for weeks.
     */
    public static final int MAX_WEEKS = 104;

    /**
     * Maximum count for months.
     */
    public static final int MAX_MONTH = 48;

    private final Reputation reputation;
    private final Commands commands;
    private final Messages messages;
    private final Users users;
    private final MetricCache cache;
    private final Service service;

    /**
     * Constructs a new MetricsRoute with the specified metrics provider.
     *
     * @param metrics the metrics provider
     */
    public MetricsRoute(de.chojo.repbot.dao.provider.Metrics metrics) {
        cache = new MetricCache();
        reputation = new Reputation(metrics, cache);
        commands = new Commands(metrics, cache);
        messages = new Messages(metrics, cache);
        users = new Users(metrics, cache);
        service = new Service(metrics, cache);
    }

    /**
     * Builds the routes for the metrics.
     */
    @Override
    public void buildRoutes() {
        cache.buildRoutes();
        reputation.buildRoutes();
        commands.buildRoutes();
        messages.buildRoutes();
        users.buildRoutes();
        service.buildRoutes();
    }
}
