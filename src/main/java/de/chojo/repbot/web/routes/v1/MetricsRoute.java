package de.chojo.repbot.web.routes.v1;

import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.routes.v1.metrics.Commands;
import de.chojo.repbot.web.routes.v1.metrics.Messages;
import de.chojo.repbot.web.routes.v1.metrics.MetricCache;
import de.chojo.repbot.web.routes.v1.metrics.Reputation;
import de.chojo.repbot.web.routes.v1.metrics.Users;
import io.swagger.v3.oas.models.parameters.Parameter;

public class MetricsRoute implements RoutesBuilder {

    public static final int MAX_DAY_OFFSET = 60;
    public static final int MAX_WEEK_OFFSET = 52;
    public static final int MAX_MONTH_OFFSET = 24;
    public static final int MAX_YEAR_OFFSET = 2;

    public static final int MAX_DAYS = 120;
    public static final int MAX_WEEKS = 104;
    public static final int MAX_MONTH = 48;
    private final Reputation reputation;
    private final Commands commands;
    private final Messages messages;
    private final Users users;
    private final MetricCache cache;

    public MetricsRoute(de.chojo.repbot.dao.provider.Metrics metrics) {
        cache = new MetricCache();
        reputation = new Reputation(metrics, cache);
        commands = new Commands(metrics, cache);
        messages = new Messages(metrics, cache);
        users = new Users(metrics, cache);
    }


    @Override
    public void buildRoutes() {
        cache.buildRoutes();
        reputation.buildRoutes();
        commands.buildRoutes();
        messages.buildRoutes();
        users.buildRoutes();
    }

    private static void offsetDoc(Parameter parameter, String resolution, int maxValue) {
        setParameter(parameter, "%s offset. 0 is current %s. Max value is %s".formatted(resolution, resolution.toLowerCase(), maxValue));
    }

    public static void offsetDayDoc(Parameter p) {
        offsetDoc(p, "Day", MAX_DAY_OFFSET);
    }

    public static void offsetWeekDoc(Parameter p) {
        offsetDoc(p, "Week", MAX_WEEK_OFFSET);
    }

    public static void offsetMonthDoc(Parameter p) {
        offsetDoc(p, "Month", MAX_MONTH_OFFSET);
    }

    public static void offsetYearDoc(Parameter p) {
        offsetDoc(p, "Year", MAX_YEAR_OFFSET);
    }

    private static void countDoc(Parameter parameter, String resolution, int maxValue) {
        setParameter(parameter, "%s count. Amount of previously %s in the chart. Max value is %s".formatted(resolution, resolution.toLowerCase(), maxValue));
    }

    public static void countDayDoc(Parameter p) {
        countDoc(p, "Days", MAX_DAYS);
    }

    public static void countWeekDoc(Parameter p) {
        countDoc(p, "Weeks", MAX_WEEKS);
    }

    public static void countMonthDoc(Parameter p) {
        countDoc(p, "Months", MAX_MONTH);
    }

    private static void setParameter(Parameter p, String description) {
        p.setDescription(description);
    }
}
