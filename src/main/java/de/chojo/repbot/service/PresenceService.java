package de.chojo.repbot.service;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.statistic.Statistic;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class PresenceService implements Runnable {
    private static final Logger log = getLogger(PresenceService.class);
    private final ShardManager shardManager;
    private final Configuration configuration;
    private final Statistic statistic;

    public PresenceService(ShardManager shardManager, Configuration configuration, Statistic statistic) {
        this.shardManager = shardManager;
        this.configuration = configuration;
        this.statistic = statistic;
    }

    public static void start(ShardManager shardManager, Configuration configuration, Statistic statistic, ScheduledExecutorService executorService) {
        var presenceService = new PresenceService(shardManager, configuration, statistic);
        if (configuration.presence().isActive()) {
            executorService.scheduleAtFixedRate(presenceService, 0, configuration.presence().interval(), TimeUnit.MINUTES);
        }
    }

    @Override
    public void run() {
        if (!configuration.presence().isActive()) return;
        refresh();
    }

    private void refresh() {
        var replacements = statistic.getSystemStatistic().replacements();
        var currentPresence = configuration.presence().randomStatus();
        var text = currentPresence.text(replacements);
        log.debug("Changed presence to: {}", text);

        shardManager.setPresence(OnlineStatus.ONLINE,
                Activity.of(currentPresence.type(), text));
    }
}
