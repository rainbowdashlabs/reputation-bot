package de.chojo.repbot.listener;

import de.chojo.repbot.util.LogNotify;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class LogListener extends ListenerAdapter implements Runnable {
    private static final Logger log = getLogger(LogListener.class);
    private final Map<Integer, Instant> disconnected = new HashMap<>();

    private LogListener() {
    }

    public static LogListener create(ScheduledExecutorService service) {
        var logListener = new LogListener();
        service.scheduleAtFixedRate(logListener, 60, 60, TimeUnit.SECONDS);
        return logListener;
    }

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        log.info(LogNotify.STATUS, "RepBot joined guild {} on shard {}.",
                event.getGuild().getId(),
                event.getJDA().getShardInfo().getShardId());
    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        log.info(LogNotify.STATUS, "RepBot left guild {} on shard {}.",
                event.getGuild().getId(),
                event.getJDA().getShardInfo().getShardId());
    }

    @Override
    public void onSessionDisconnect(@NotNull SessionDisconnectEvent event) {
        disconnected.put(event.getJDA().getShardInfo().getShardId(), Instant.now());
        log.debug("Shard {} disconnected.", event.getJDA().getShardInfo().getShardId());
    }

    @Override
    public void onSessionRecreate(@NotNull SessionRecreateEvent event) {
        handleShardReconnect(event.getJDA());
    }

    @Override
    public void onSessionResume(@NotNull SessionResumeEvent event) {
        handleShardReconnect(event.getJDA());
    }

    private void handleShardReconnect(JDA jda) {
        var shardId = jda.getShardInfo().getShardId();
        var seconds = Duration.between(
                                      disconnected.getOrDefault(shardId, Instant.now()), Instant.now())
                              .getSeconds();
        disconnected.remove(shardId);
        if (seconds > 5) {
            log.info(LogNotify.STATUS,
                    "Shard {} was disconnected for {} seconds. Everything is fine.",
                    shardId, seconds);
        } else {
            log.debug("Shard {} reconnected", jda.getShardInfo().getShardId());
        }
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        handleShardReconnect(event.getJDA());
        log.info(LogNotify.STATUS, "Shard {}/{} started. Shard is connected to {} guilds.",
                event.getJDA().getShardInfo().getShardId() + 1,
                event.getJDA().getShardManager().getShardsTotal(),
                event.getGuildTotalCount());
    }

    @Override
    public void run() {
        if (disconnected.isEmpty()) return;

        var message = disconnected.entrySet().stream()
                                  .map(e -> {
                                      var seconds = Duration.between(e.getValue(), Instant.now()).getSeconds();
                                      if (seconds < 5) return null;
                                      return String.format(" Shard %d is disconnected since %d seconds", e.getKey(), seconds);
                                  })
                                  .filter(Objects::nonNull)
                                  .collect(Collectors.joining("\n"));
        if (message.isBlank()) return;

        log.warn(LogNotify.NOTIFY_ADMIN, message);
    }
}
