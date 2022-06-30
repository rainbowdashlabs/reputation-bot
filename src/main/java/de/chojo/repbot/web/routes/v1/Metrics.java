package de.chojo.repbot.web.routes.v1;

import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.routes.v1.metrics.Commands;
import de.chojo.repbot.web.routes.v1.metrics.Messages;
import de.chojo.repbot.web.routes.v1.metrics.Reputation;
import de.chojo.repbot.web.routes.v1.metrics.Users;

public class Metrics implements RoutesBuilder {
    private final Reputation reputation;
    private final Commands commands;
    private final Messages messages;
    private final Users users;

    public Metrics(de.chojo.repbot.dao.provider.Metrics metrics) {
        reputation = new Reputation(metrics);
        commands = new Commands(metrics);
        messages = new Messages(metrics);
        users = new Users(metrics);
    }


    @Override
    public void buildRoutes() {
        reputation.buildRoutes();
        commands.buildRoutes();
        messages.buildRoutes();
        users.buildRoutes();
    }
}
