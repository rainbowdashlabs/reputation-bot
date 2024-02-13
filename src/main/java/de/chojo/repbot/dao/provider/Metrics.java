/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.provider;

import de.chojo.repbot.dao.access.metrics.Commands;
import de.chojo.repbot.dao.access.metrics.Messages;
import de.chojo.repbot.dao.access.metrics.Reputation;
import de.chojo.repbot.dao.access.metrics.Service;
import de.chojo.repbot.dao.access.metrics.Statistic;
import de.chojo.repbot.dao.access.metrics.Users;

import javax.sql.DataSource;

public class Metrics {
    private final Commands commands;
    private final Messages messages;
    private final Users users;
    private final Statistic statistic;
    private final Reputation reputation;
    private final Service service;

    public Metrics(DataSource dataSource) {
        commands = new Commands();
        messages = new Messages();
        users = new Users();
        statistic = new Statistic();
        reputation = new Reputation();
        service = new Service();
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
