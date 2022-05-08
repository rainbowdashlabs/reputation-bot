package de.chojo.repbot.service;

import de.chojo.repbot.dao.access.Gdpr;
import de.chojo.repbot.dao.access.gdpr.RemovalTask;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
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
    private final Guilds guilds;
    private final Gdpr gdpr;

    private GdprService(ShardManager shardManager, Guilds guilds, Gdpr gdpr, ExecutorService executorService) {
        this.shardManager = shardManager;
        this.guilds = guilds;
        this.gdpr = gdpr;
        this.executorService = executorService;
    }

    public static GdprService of(ShardManager shardManager, Guilds guilds, Gdpr gdpr,
                                 ScheduledExecutorService scheduledExecutorService) {
        var service = new GdprService(shardManager, guilds, gdpr, scheduledExecutorService);
        scheduledExecutorService.scheduleAtFixedRate(service, 10, 60, TimeUnit.MINUTES);
        return service;
    }

    @Override
    public void run() {
        var reportRequests = gdpr.getReportRequests();

        for (var request : reportRequests) {
            var send = request.sendData(shardManager);
            if (send) {
                request.requestSend();
            } else {
                request.requestSendFailed();
            }
        }

        gdpr.cleanupRequests();

        gdpr.getRemovalTasks().forEach(RemovalTask::executeRemovalTask);
    }


    public CompletableFuture<Integer> cleanupGuildUsers(Guild guild) {
        return CompletableFuture.supplyAsync(() -> {
            var savedIds = guilds.guild(guild).userIds();
            var memberIds = guild.loadMembers().get()
                    .stream()
                    .map(ISnowflake::getIdLong).toList();
            var collect = savedIds.stream().filter(id -> !memberIds.contains(id)).toList();
            for (var id : collect) RemovalTask.anonymExecute(gdpr, guild.getIdLong(), id);
            return collect.size();
        }, executorService);
    }

    public void cleanupGuildUser(Guild guild, Long user) {
        CompletableFuture.runAsync(() -> RemovalTask.anonymExecute(gdpr, guild.getIdLong(), user), executorService);
    }
}
