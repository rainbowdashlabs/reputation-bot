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

/**
 * Provides access to various metrics.
 */
public class Metrics {
    private final Commands commands;
    private final Messages messages;
    private final Users users;
    private final Statistic statistic;
    private final Reputation reputation;
    private final Service service;

    /**
     * Constructs a new Metrics provider with the specified data source.
     *
     * @param dataSource the data source to use
     */
    public Metrics(DataSource dataSource) {
        commands = new Commands();
        messages = new Messages();
        users = new Users();
        statistic = new Statistic();
        reputation = new Reputation();
        service = new Service();
    }

    /**
     * Returns the Commands metrics.
     *
     * @return the Commands metrics
     */
    public Commands commands() {
        return commands;
    }

    /**
     * Returns the Messages metrics.
     *
     * @return the Messages metrics
     */
    public Messages messages() {
        return messages;
    }

    /**
     * Returns the Users metrics.
     *
     * @return the Users metrics
     */
    public Users users() {
        return users;
    }

    /**
     * Returns the Statistic metrics.
     *
     * @return the Statistic metrics
     */
    public Statistic statistic() {
        return statistic;
    }

    /**
     * Returns the Reputation metrics.
     *
     * @return the Reputation metrics
     */
    public Reputation reputation() {
        return reputation;
    }

    /**
     * Returns the Service metrics.
     *
     * @return the Service metrics
     */
    public Service service() {
        return service;
    }
}
