package de.chojo.repbot.service;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.statistic.Statistic;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PresenceService implements Runnable {
    private final ShardManager shardManager;
    private final Configuration configuration;
    private final Statistic statistic;
    private String currentPresence = "";

    public PresenceService(ShardManager shardManager, Configuration configuration, Statistic statistic) {
        this.shardManager = shardManager;
        this.configuration = configuration;
        this.statistic = statistic;
    }

    private OnlineStatus status(int value) {
        return OnlineStatus.ONLINE;
    }

    private Activity activity(int value) {
        return Activity.of(Activity.ActivityType.CUSTOM_STATUS, currentPresence);
    }

    private OnlineStatus status() {
        return OnlineStatus.ONLINE;
    }

    private Activity activity() {
        return Activity.of(Activity.ActivityType.CUSTOM_STATUS, currentPresence);
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

        List<Replacement> replacements = new ArrayList<>();
        replacements.addAll(Arrays.asList(systemStatistic.replacements()));
        replacements.addAll(Arrays.asList(systemStatistic.aggregatedShards().replacements()));
        replacements.addAll(Arrays.asList(systemStatistic.dataStatistic().replacements()));
        replacements.addAll(Arrays.asList(systemStatistic.processStatistics().replacements()));

        currentPresence = configuration.presence().randomStatus();

        for (var replacement : replacements) {
            currentPresence = replacement.invoke(currentPresence);
        }

        shardManager.setPresence(status(), activity());
        shardManager.setPresenceProvider(this::status, this::activity);
    }
}
