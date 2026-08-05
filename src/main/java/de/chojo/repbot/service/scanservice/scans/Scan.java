/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.scanservice.scans;

import de.chojo.repbot.service.scanservice.ScanProcess;
import de.chojo.repbot.web.pojo.scan.ScanProgress;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public interface Scan {
    Logger log = getLogger(Scan.class);

    static Optional<Scan> create(ScanProcess scanProcess, GuildChannel channel) {
        if (!channel.getGuild()
                .getSelfMember()
                .hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY)) {
            log.info(
                    "Skipping channel {} ({}) on guild {} during scan. Missing view channel or message history permission.",
                    channel.getName(),
                    channel.getId(),
                    channel.getGuild().getId());
            return Optional.empty();
        }
        return switch (channel.getType()) {
            case TEXT, NEWS -> Optional.of(TextChannelScan.create(scanProcess, (StandardGuildMessageChannel) channel));
            case CATEGORY -> Optional.of(CategoryScan.create(scanProcess, (Category) channel));
            case STAGE, VOICE -> Optional.of(ChannelScan.create(scanProcess, (GuildMessageChannel) channel));
            case GUILD_NEWS_THREAD, GUILD_PRIVATE_THREAD, GUILD_PUBLIC_THREAD ->
                Optional.of(ChannelScan.create(scanProcess, (ThreadChannel) channel));
            case FORUM, MEDIA -> Optional.of(ThreadContainerScan.create(scanProcess, (IPostContainer) channel));
            default -> {
                log.info(
                        "Skipping channel {} ({}) on guild {} during scan. Unsupported channel type {}.",
                        channel.getName(),
                        channel.getId(),
                        channel.getGuild().getId(),
                        channel.getType());
                yield Optional.empty();
            }
        };
    }

    default void scan() {
        next().ifPresent(Scan::scan);
    }

    default Optional<? extends Scan> next() {
        return scans().stream().filter(c -> !c.done()).findFirst();
    }

    default boolean done() {
        return scans().stream().allMatch(Scan::done);
    }

    default int hits() {
        return scans().stream().mapToInt(Scan::hits).sum();
    }

    default int scanned() {
        return scans().stream().mapToInt(Scan::scanned).sum();
    }

    default int maxMessages() {
        return scans().stream().mapToInt(Scan::maxMessages).sum();
    }

    List<? extends Scan> scans();

    ScanProgress progress();
}
