/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.core.Threading;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.scanservice.ScanProcess;
import de.chojo.repbot.util.LogNotify;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class ScanService {
    private static final Logger log = getLogger(ScanService.class);
    private final GuildRepository guildRepository;
    private final Threading threading;
    private MessageAnalyzer analyzer;
    private final Map<Long, ScanProcess> scanProcesses = new HashMap<>();

    public ScanService(GuildRepository guildRepository, Threading threading) {
        this.guildRepository = guildRepository;
        this.threading = threading;
        threading.repBotWorker().schedule(this::tick, 2, TimeUnit.SECONDS);
    }

    public void scan(Guild guild) {
        if (scanProcesses.containsKey(guild.getIdLong())) return;
        RepGuild repGuild = guildRepository.guild(guild);
        List<Channel> channels =
                new ArrayList<>(repGuild.settings().thanking().channels().channels());
        channels.addAll(repGuild.settings().thanking().channels().categories());
        ScanProcess scanProcess = new ScanProcess(analyzer, repGuild, channels);
        scanProcess.init();
        scanProcesses.put(guild.getIdLong(), scanProcess);
        log.info("Started scan for {}", guild);
    }

    private void tick() {
        for (ScanProcess process : List.copyOf(scanProcesses.values())) {
            try {
                process.scan();
            } catch (Exception e) {
                log.error(LogNotify.NOTIFY_ADMIN, "Critical error while scanning", e);
            }
            if (process.done()) {
                ScanProcess remove = scanProcesses.remove(process.guild().getIdLong());
                log.info("Finished scan on {}", remove.guild());
                remove.save();
            }
        }
        threading.repBotWorker().schedule(this::tick, 2, TimeUnit.SECONDS);
    }

    public Optional<ScanProcess> getScanProcess(Guild guild) {
        return Optional.ofNullable(scanProcesses.get(guild.getIdLong()));
    }

    public void addAnalyzer(MessageAnalyzer analyzer) {
        this.analyzer = analyzer;
    }
}
