/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.dao.access.Gdpr;
import de.chojo.repbot.dao.access.gdpr.RemovalTask;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for handling GDPR-related tasks.
 */
public class GdprService implements Runnable {
    private static final Logger log = getLogger(GdprService.class);
    private final ShardManager shardManager;
    private final ExecutorService executorService;
    private final Guilds guilds;
    private final Gdpr gdpr;

    /**
     * Constructs a new GdprService instance.
     *
     * @param shardManager the ShardManager instance
     * @param guilds the Guilds instance
     * @param gdpr the Gdpr instance
     * @param executorService the ExecutorService instance
     */
    private GdprService(ShardManager shardManager, Guilds guilds, Gdpr gdpr, ExecutorService executorService) {
        this.shardManager = shardManager;
        this.guilds = guilds;
        this.gdpr = gdpr;
        this.executorService = executorService;
    }

    /**
     * Creates and schedules a new GdprService instance.
     *
     * @param shardManager the ShardManager instance
     * @param guilds the Guilds instance
     * @param gdpr the Gdpr instance
     * @param scheduledExecutorService the ScheduledExecutorService instance
     * @return the created GdprService instance
     */
    public static GdprService of(ShardManager shardManager, Guilds guilds, Gdpr gdpr,
                                 ScheduledExecutorService scheduledExecutorService) {
        var service = new GdprService(shardManager, guilds, gdpr, scheduledExecutorService);
        scheduledExecutorService.scheduleAtFixedRate(service, 15, 3600, TimeUnit.SECONDS);
        return service;
    }

    /**
     * Runs the GDPR service tasks.
     */
    @Override
    public void run() {
        var reportRequests = gdpr.getReportRequests(shardManager);

        for (var request : reportRequests) {
            var send = request.sendData();
            if (send) {
                request.requestSend();
            } else {
                request.requestSendFailed();
            }
        }

        gdpr.cleanupRequests();

        gdpr.getRemovalTasks().forEach(RemovalTask::executeRemovalTask);

        cleanupGuilds();
    }

    /**
     * Cleans up user data for a specific guild.
     *
     * @param guild the Guild instance
     * @return a CompletableFuture with the number of pruned users
     */
    public CompletableFuture<Integer> cleanupGuildUsers(Guild guild) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Guild prune was started on {}", guild.getId());
            var pruned = 0;
            var users = guilds.guild(guild).userIds();
            log.info("Checking {} users", users.size());
            for (var user : users) {
                try {
                    guild.retrieveMemberById(user).complete();
                } catch (RuntimeException e) {
                    log.info("Removing user {} data during guild prune", user);
                    RemovalTask.anonymExecute(guild.getIdLong(), user);
                    pruned++;
                }
            }
            log.info("Prune on guild {} finished. Removed {} users", guild.getId(), pruned);
            return pruned;
        }, executorService);
    }

    /**
     * Cleans up user data for a specific user in a guild.
     *
     * @param guild the Guild instance
     * @param user the user ID
     */
    public void cleanupGuildUser(Guild guild, Long user) {
        log.info("User data of {} was pruned on guild {}.", user, guild.getIdLong());
        CompletableFuture.runAsync(() -> RemovalTask.anonymExecute(guild.getIdLong(), user), executorService);
    }

    /**
     * Cleans up guilds by checking their status and updating their GDPR deletion queue.
     */
    private void cleanupGuilds() {
        for (var page : guilds.guilds(100)) {
            for (var guild : page) {
                if (shardManager.getGuildById(guild.guildId()) != null) {
                    guild.gdpr().dequeueDeletion();
                } else {
                    guild.gdpr().queueDeletion();
                }
            }
        }
    }
}
