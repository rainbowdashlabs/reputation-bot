package de.chojo.repbot.service;

import de.chojo.repbot.data.GdprData;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.RemovalTask;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class GdprService implements Runnable {
    private static final Logger log = getLogger(GdprService.class);
    private final ShardManager shardManager;
    private final GdprData gdprData;
    private final GuildData guildData;
    private final ExecutorService executorService;

    private GdprService(ShardManager shardManager, DataSource dataSource, ExecutorService executorService) {
        this.shardManager = shardManager;
        gdprData = new GdprData(dataSource);
        guildData = new GuildData(dataSource);
        this.executorService = executorService;
    }

    public static GdprService of(ShardManager shardManager, DataSource data,
                                 ScheduledExecutorService scheduledExecutorService) {
        var service = new GdprService(shardManager, data, scheduledExecutorService);
        scheduledExecutorService.scheduleAtFixedRate(service, 10, 60, TimeUnit.MINUTES);
        return service;
    }

    @Override
    public void run() {
        var reportRequests = gdprData.getReportRequests();

        for (var id : reportRequests) {
            var result = resolveUserRequest(id);
            if (result) {
                gdprData.markAsSend(id);
            } else {
                gdprData.markAsFailed(id);
            }
        }

        gdprData.cleanupRequests();

        gdprData.getRemovalTasks().forEach(gdprData::executeRemovalTask);
    }

    private boolean resolveUserRequest(Long userId) {
        User user;
        try {
            user = shardManager.retrieveUserById(userId).complete();
        } catch (RuntimeException e) {
            log.info("Could not process gdpr request for user {}. User could not be retrieved.", userId);
            return false;
        }
        if (user == null) {
            log.info("Could not process gdpr request for user {}. User could not be retrieved.", userId);
            return false;
        }

        PrivateChannel privateChannel;
        try {
            privateChannel = user.openPrivateChannel().complete();
        } catch (RuntimeException e) {
            log.info("Could not process gdpr request for user {}. Could not open private channel.", userId);
            return false;
        }
        if (privateChannel == null) {
            log.info("Could not process gdpr request for user {}. Could not open private channel.", userId);
            return false;
        }

        var userData = gdprData.getUserData(user);

        if (userData.isEmpty()) {
            log.warn("Could not process gdpr request for user {}. Data aggregation failed.", userId);
            return false;
        }
        Path tempFile;
        try {
            tempFile = Files.createTempFile("repbot_gdpr", ".json");
        } catch (IOException e) {
            log.warn("Coult not create temp file", e);
            return false;
        }
        try {
            Files.writeString(tempFile, userData.get());
        } catch (IOException e) {
            log.warn("Could not write to temp file", e);
            return false;
        }

        try {
            privateChannel
                    .sendMessage("Here is your requested data. You can request it again in 30 days.")
                    .addFile(tempFile.toFile())
                    .complete();
        } catch (RuntimeException e) {
            log.warn("Could not send gdpr data to user {}. File sending failed.", userId, e);
            return false;
        }
        return true;
    }

    public CompletableFuture<Integer> cleanupGuildUsers(Guild guild) {
        return CompletableFuture.supplyAsync(() -> {
            var savedIds = guildData.guildUserIds(guild);
            var memberIds = guild.loadMembers().get()
                    .stream()
                    .map(ISnowflake::getIdLong)
                    .collect(Collectors.toList());
            var collect = savedIds.stream().filter(id -> !memberIds.contains(id)).collect(Collectors.toList());
            for (var id : collect) gdprData.executeRemovalTask(new RemovalTask(-1, guild.getIdLong(), id));
            return collect.size();
        }, executorService);
    }

    public CompletableFuture<Void> cleanupGuildUser(Guild guild, Long user) {
        return CompletableFuture.runAsync(() ->
                        gdprData.executeRemovalTask(new RemovalTask(-1, guild.getIdLong(), user)),
                executorService);
    }
}
