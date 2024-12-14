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
import io.swagger.v3.oas.models.parameters.Parameter;

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

    /**
     * Sets the documentation for the offset parameter.
     *
     * @param parameter the parameter to set the documentation for
     * @param resolution the resolution of the offset
     * @param maxValue the maximum value for the offset
     */
    private static void offsetDoc(Parameter parameter, String resolution, int maxValue) {
        setParameter(parameter, "%s offset. 0 is current %s. Max value is %s".formatted(resolution, resolution.toLowerCase(), maxValue));
    }

    /**
     * Sets the documentation for the day offset parameter.
     *
     * @param p the parameter to set the documentation for
     */
    public static void offsetDayDoc(Parameter p) {
        offsetDoc(p, "Day", MAX_DAY_OFFSET);
    }

    /**
     * Sets the documentation for the hour offset parameter.
     *
     * @param p the parameter to set the documentation for
     */
    public static void offsetHourDoc(Parameter p) {
        offsetDoc(p, "Hour", MAX_HOUR_OFFSET);
    }

    /**
     * Sets the documentation for the week offset parameter.
     *
     * @param p the parameter to set the documentation for
     */
    public static void offsetWeekDoc(Parameter p) {
        offsetDoc(p, "Week", MAX_WEEK_OFFSET);
    }

    /**
     * Sets the documentation for the month offset parameter.
     *
     * @param p the parameter to set the documentation for
     */
    public static void offsetMonthDoc(Parameter p) {
        offsetDoc(p, "Month", MAX_MONTH_OFFSET);
    }

    /**
     * Sets the documentation for the year offset parameter.
     *
     * @param p the parameter to set the documentation for
     */
    public static void offsetYearDoc(Parameter p) {
        offsetDoc(p, "Year", MAX_YEAR_OFFSET);
    }

    /**
     * Sets the documentation for the count parameter.
     *
     * @param parameter the parameter to set the documentation for
     * @param resolution the resolution of the count
     * @param maxValue the maximum value for the count
     */
    private static void countDoc(Parameter parameter, String resolution, int maxValue) {
        setParameter(parameter, "%s count. Amount of previously %s in the chart. Max value is %s".formatted(resolution, resolution.toLowerCase(), maxValue));
    }

    /**
     * Sets the documentation for the hour count parameter.
     *
     * @param p the parameter to set the documentation for
     */
    public static void countHourDoc(Parameter p) {
        countDoc(p, "Hours", MAX_HOURS);
    }

    /**
     * Sets the documentation for the day count parameter.
     *
     * @param p the parameter to set the documentation for
     */
    public static void countDayDoc(Parameter p) {
        countDoc(p, "Days", MAX_DAYS);
    }

    /**
     * Sets the documentation for the week count parameter.
     *
     * @param p the parameter to set the documentation for
     */
    public static void countWeekDoc(Parameter p) {
        countDoc(p, "Weeks", MAX_WEEKS);
    }

    /**
     * Sets the documentation for the month count parameter.
     *
     * @param p the parameter to set the documentation for
     */
    public static void countMonthDoc(Parameter p) {
        countDoc(p, "Months", MAX_MONTH);
    }

    /**
     * Sets the description for the parameter.
     *
     * @param p the parameter to set the description for
     * @param description the description to set
     */
    private static void setParameter(Parameter p, String description) {
        p.setDescription(description);
    }
}
