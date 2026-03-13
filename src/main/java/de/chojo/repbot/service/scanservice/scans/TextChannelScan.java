/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.scanservice.scans;

import de.chojo.repbot.service.scanservice.ScanProcess;
import de.chojo.repbot.web.pojo.scan.ScanProgress;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

import java.util.List;

import static de.chojo.repbot.web.pojo.scan.ScanTarget.fromChannelType;

public class TextChannelScan implements Scan {
    private final StandardGuildMessageChannel channel;
    private final ThreadContainerScan threadContainerScan;
    private final ChannelScan channelScan;

    public TextChannelScan(
            StandardGuildMessageChannel channel, ThreadContainerScan threadContainerScan, ChannelScan channelScan) {
        this.channel = channel;
        this.threadContainerScan = threadContainerScan;
        this.channelScan = channelScan;
    }

    public static TextChannelScan create(ScanProcess scanProcess, StandardGuildMessageChannel channel) {
        return new TextChannelScan(
                channel, ThreadContainerScan.create(scanProcess, channel), ChannelScan.create(scanProcess, channel));
    }

    @Override
    public List<Scan> scans() {
        return List.of(threadContainerScan, channelScan);
    }

    @Override
    public ScanProgress progress() {
        return new ScanProgress(
                fromChannelType(channel.getType()),
                channel.getIdLong(),
                channel.getName(),
                scanned(),
                maxMessages(),
                hits(),
                threadContainerScan.progress().childs());
    }
}
