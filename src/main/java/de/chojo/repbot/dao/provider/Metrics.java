package de.chojo.repbot.dao.provider;

import de.chojo.repbot.dao.access.metrics.Commands;
import de.chojo.repbot.dao.access.metrics.Messages;
import de.chojo.repbot.dao.access.metrics.Reputation;
import de.chojo.repbot.dao.access.metrics.Service;
import de.chojo.repbot.dao.access.metrics.Statistic;
import de.chojo.repbot.dao.access.metrics.Users;
import de.chojo.sadu.base.QueryFactory;

import javax.sql.DataSource;

public class Metrics extends QueryFactory {
    private final Commands commands;
    private final Messages messages;
    private final Users users;
    private final Statistic statistic;
    private final Reputation reputation;
    private final Service service;

    public Metrics(DataSource dataSource) {
        super(dataSource);
        commands = new Commands(this);
        messages = new Messages(this);
        users = new Users(this);
        statistic = new Statistic(this);
        reputation = new Reputation(this);
        service = new Service(this);
    }

    public Commands commands() {
        return commands;
    }

    public Messages messages() {
        return messages;
    }

    public Users users() {
        return users;
    }

    public Statistic statistic() {
        return statistic;
    }

    public Reputation reputation() {
        return reputation;
    }

    public Service service() {
        return service;
    }
}
