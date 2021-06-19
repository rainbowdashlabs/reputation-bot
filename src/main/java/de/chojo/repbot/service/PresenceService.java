package de.chojo.repbot.service;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.PresenceSettings;
import de.chojo.repbot.statistic.Statistic;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class PresenceService implements Runnable {
    private static final Logger log = getLogger(PresenceService.class);
    private final ShardManager shardManager;
    private final Configuration configuration;
    private final Statistic statistic;
    private List<Replacement> replacements = new ArrayList<>();

    public PresenceService(ShardManager shardManager, Configuration configuration, Statistic statistic) {
        this.shardManager = shardManager;
        this.configuration = configuration;
        this.statistic = statistic;
    }

    public static PresenceService start(ShardManager shardManager, Configuration configuration, Statistic statistic, ScheduledExecutorService executorService) {
        var presenceService = new PresenceService(shardManager, configuration, statistic);
        if (configuration.presence().isActive()) {
            executorService.scheduleAtFixedRate(presenceService, 0, configuration.presence().interval(), TimeUnit.MINUTES);
        }
        return presenceService;
    }

    @Override
    public void run() {
        if (!configuration.presence().isActive()) return;
        refresh();
    }

    private void refresh() {
        var systemStatistic = statistic.getSystemStatistic();

        replacements.clear();
        replacements.addAll(Arrays.asList(systemStatistic.replacements()));
        replacements.addAll(Arrays.asList(systemStatistic.aggregatedShards().replacements()));
        replacements.addAll(Arrays.asList(systemStatistic.dataStatistic().replacements()));
        replacements.addAll(Arrays.asList(systemStatistic.processStatistics().replacements()));

        var currentPresence = configuration.presence().randomStatus();
        var text = currentPresence.text(replacements);
        log.debug("Changed presence to: {}", text);

        shardManager.setPresence(OnlineStatus.ONLINE,
                Activity.of(currentPresence.type(), text));
    }
}
