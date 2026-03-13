/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.scanservice.scans;

import de.chojo.repbot.service.scanservice.ScanProcess;
import de.chojo.repbot.web.pojo.scan.ScanProgress;
import de.chojo.repbot.web.pojo.scan.ScanTarget;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThreadContainerScan implements Scan {
    private ScanProcess scanProcess;
    private final IThreadContainer postContainer;
    private final List<ChannelScan> channels;

    public ThreadContainerScan(List<ChannelScan> channels, IThreadContainer postContainer) {
        this.channels = channels;
        this.postContainer = postContainer;
    }

    public static ThreadContainerScan create(ScanProcess scanProcess, IThreadContainer postContainer) {
        Set<GuildMessageChannel> threads = new HashSet<>();
        postContainer.retrieveArchivedPublicThreadChannels().stream().forEach(threads::add);
        threads.addAll(postContainer.getThreadChannels());
        List<ChannelScan> list =
                threads.stream().map(c -> ChannelScan.create(scanProcess, c)).toList();
        return new ThreadContainerScan(list, postContainer);
    }

    @Override
    public List<? extends Scan> scans() {
        return channels;
    }

    @Override
    public ScanProgress progress() {
        return new ScanProgress(
                ScanTarget.fromChannelType(postContainer.getType()),
                postContainer.getIdLong(),
                postContainer.getName(),
                scanned(),
                maxMessages(),
                hits(),
                channels.stream().map(Scan::progress).toList());
    }
}
