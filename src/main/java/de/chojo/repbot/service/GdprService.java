/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.util.SysVar;
import de.chojo.repbot.dao.access.Gdpr;
import de.chojo.repbot.dao.access.gdpr.RemovalTask;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class GdprService implements Runnable {
    private static final Logger log = getLogger(GdprService.class);
    private final ShardManager shardManager;
    private final ExecutorService executorService;
    private final GuildRepository guildRepository;
    private final Gdpr gdpr;

    private GdprService(ShardManager shardManager, GuildRepository guildRepository, Gdpr gdpr, ExecutorService executorService) {
        this.shardManager = shardManager;
        this.guildRepository = guildRepository;
        this.gdpr = gdpr;
        this.executorService = executorService;
    }

    public static GdprService of(ShardManager shardManager, GuildRepository guildRepository, Gdpr gdpr,
                                 ScheduledExecutorService scheduledExecutorService) {
        var service = new GdprService(shardManager, guildRepository, gdpr, scheduledExecutorService);
        scheduledExecutorService.scheduleAtFixedRate(service, 15, 3600, TimeUnit.SECONDS);
        return service;
    }

    @Override
    public void run() {
        if (SysVar.envOrProp("BOT_GDPR_ENABLE", "bot.gdpr.enable", "true").equals("false")) return;
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


    public CompletableFuture<Integer> cleanupGuildUsers(Guild guild) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Guild prune was started on {}", guild.getId());
            var pruned = 0;
            var users = guildRepository.guild(guild).userIds();
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

    public void cleanupGuildUser(Guild guild, Long user) {
        log.info("User data of {} was pruned on guild {}.", user, guild.getIdLong());
        CompletableFuture.runAsync(() -> RemovalTask.anonymExecute(guild.getIdLong(), user), executorService);
    }

    private void cleanupGuilds() {
        for (var page : guildRepository.guilds(100)) {
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
