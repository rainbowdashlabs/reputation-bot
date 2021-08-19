package de.chojo.repbot.statistic;

import de.chojo.repbot.data.StatisticData;
import de.chojo.repbot.statistic.element.DataStatistic;
import de.chojo.repbot.statistic.element.ProcessStatistics;
import de.chojo.repbot.statistic.element.ShardStatistic;
import de.chojo.repbot.statistic.element.SystemStatistics;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Statistic implements Runnable {
    private static final Logger log = getLogger(Statistic.class);
    private final Map<Integer, long[]> analyzedMessages = new HashMap<>();
    private final ShardManager shardManager;
    private final StatisticData statisticData;
    private int currentMin;

    private Statistic(ShardManager shardManager, DataSource dataSource) {
        this.shardManager = shardManager;
        this.statisticData = new StatisticData(dataSource);
        getSystemStatistic();
    }

    public static Statistic of(ShardManager shardManager, DataSource dataSource, ScheduledExecutorService service) {
        var statistic = new Statistic(shardManager, dataSource);
        service.scheduleAtFixedRate(statistic, 0, 1, TimeUnit.MINUTES);
        service.scheduleAtFixedRate(statistic::refreshStatistics, 1, 30, TimeUnit.MINUTES);
        return statistic;
    }

    private int minute() {
        return LocalTime.now().getMinute();
    }

    public void messageAnalyzed(JDA shard) {
        var shardId = shard.getShardInfo().getShardId();
        getShardMessageStats(shardId)[currentMin]++;
    }

    private long[] getShardMessageStats(int shardId) {
        return analyzedMessages.computeIfAbsent(shardId, k -> new long[60]);
    }

    @Override
    public void run() {
        currentMin = minute();
        resetMinute(analyzedMessages);
    }

    private void resetMinute(Map<Integer, long[]> map) {
        for (var shardId = 0; shardId < shardManager.getShardsTotal(); shardId++) {
            map.getOrDefault(shardId, new long[60])[currentMin] = 0;
        }
    }

    private ShardStatistic getShardStatistic(JDA jda) throws ExecutionException {
        var shardId = jda.getShardInfo().getShardId();
        var analyzedMessages = arraySum(getShardMessageStats(shardId));

        return new ShardStatistic(
                shardId + 1,
                jda.getStatus(),
                analyzedMessages);
    }

    public SystemStatistics getSystemStatistic() {
        var shardStatistics = shardManager.getShardCache()
                .stream().map(jda -> {
                    try {
                        return getShardStatistic(jda);
                    } catch (ExecutionException e) {
                        log.error("An error occured while building the system statistics", e);
                    }
                    return new ShardStatistic(jda.getShardInfo().getShardId(),
                            JDA.Status.DISCONNECTED, 0);
                }).collect(Collectors.toList());

        return new SystemStatistics(ProcessStatistics.create(),
                statisticData.getStatistic().orElseGet(DataStatistic::new),
                shardStatistics);
    }

    private long arraySum(long[] array) {
        return Arrays.stream(array).sum();
    }

    private void refreshStatistics() {
        statisticData.refreshStatistics();
    }
}
