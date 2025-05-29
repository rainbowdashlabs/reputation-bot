/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.core.Threading;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshInterval;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class AutopostService {
    private static final Logger log = getLogger(AutopostService.class);
    private final ShardManager shardManager;
    private final GuildRepository guildRepository;

    public AutopostService(ShardManager shardManager, GuildRepository guildRepository, Threading threading) {
        this.shardManager = shardManager;
        this.guildRepository = guildRepository;
        threading.repBotWorker().scheduleAtFixedRate(this::check, 60 - Instant.now().get(ChronoField.MINUTE_OF_HOUR), 60, TimeUnit.MINUTES);
    }

    private void check() {
        log.info("Refreshing autoposts.");
        Instant now = Instant.now();
        for (RefreshInterval value : RefreshInterval.values()) {
            if (value.isApplicable(now)) {
                guildRepository.byAutopostRefreshInterval(value).forEach(this::update);
            }
        }
    }

    public void update(RepGuild guild) {
        if (guild.isById()) return;
        guild.load(shardManager);
        // TODO: Send/refresh/save message
    }
}
