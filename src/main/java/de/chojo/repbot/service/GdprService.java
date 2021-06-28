package de.chojo.repbot.service;

import de.chojo.repbot.data.GdprData;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class GdprService implements Runnable {
    private static final Logger log = getLogger(GdprService.class);
    private final ShardManager shardManager;
    private final GdprData data;

    private GdprService(ShardManager shardManager, DataSource data) {
        this.shardManager = shardManager;
        this.data = new GdprData(data);
    }

    public static GdprService of(ShardManager shardManager, DataSource data, ScheduledExecutorService executorService) {
        var service = new GdprService(shardManager, data);
        executorService.scheduleAtFixedRate(service, 10, 60, TimeUnit.MINUTES);
        return service;
    }

    @Override
    public void run() {
        var reportRequests = data.getReportRequests();

        for (var id : reportRequests) {
            var result = resolveUserRequest(id);
            if (result) {
                data.markAsSend(id);
            } else {
                data.markAsFailed(id);
            }
        }

        data.cleanupRequests();

        data.getRemovalTasks().forEach(data::executeRemovalTask);
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

        var userData = data.getUserData(user);

        if (userData.isEmpty()) {
            log.info("Could not process gdpr request for user {}. Data aggregation failed.", userId);
            return false;
        }
        Path tempFile;
        try {
            tempFile = Files.createTempFile("repbot_gdpr", ".json");
        } catch (IOException e) {
            log.info("Coult not create temp file", e);
            return false;
        }
        try {
            Files.writeString(tempFile, userData.get());
        } catch (IOException e) {
            log.info("Could not write to temp file", e);
            return false;
        }

        try {
            privateChannel
                    .sendMessage("Here is your requested data. You can request it again in 30 days.")
                    .addFile(tempFile.toFile())
                    .complete();
        } catch (RuntimeException e) {
            log.info("Could not send gdpr data to user {}. File sending failed.", userId);
            return false;
        }
        return true;
    }
}
