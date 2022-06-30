package de.chojo.repbot.web.routes.v1;

import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.routes.v1.metrics.Commands;
import de.chojo.repbot.web.routes.v1.metrics.Messages;
import de.chojo.repbot.web.routes.v1.metrics.MetricCache;
import de.chojo.repbot.web.routes.v1.metrics.Reputation;
import de.chojo.repbot.web.routes.v1.metrics.Users;

import static io.javalin.apibuilder.ApiBuilder.after;
import static io.javalin.apibuilder.ApiBuilder.before;

public class Metrics implements RoutesBuilder {

    public static final int MAX_DAY_OFFSET = 30;
    public static final int MAX_WEEK_OFFSET = 24;
    public static final int MAX_MONTH_OFFSET = 24;
    public static final int MAX_YEAR_OFFSET = 2;

    public static final int MAX_DAYS = 90;
    public static final int MAX_WEEKS = 52;
    public static final int MAX_MONTH = 24;
    private final Reputation reputation;
    private final Commands commands;
    private final Messages messages;
    private final Users users;
        private final MetricCache cache;

    public Metrics(de.chojo.repbot.dao.provider.Metrics metrics) {
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

}
