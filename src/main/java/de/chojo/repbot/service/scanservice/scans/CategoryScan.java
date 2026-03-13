/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.scanservice.scans;

import de.chojo.repbot.service.scanservice.ScanProcess;
import de.chojo.repbot.web.pojo.scan.ScanProgress;
import de.chojo.repbot.web.pojo.scan.ScanTarget;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

import java.util.List;
import java.util.Optional;

public class CategoryScan implements Scan {
    private final Category category;
    private final List<Scan> channels;

    public CategoryScan(Category category, List<Scan> channels) {
        this.category = category;
        this.channels = channels;
    }

    public static CategoryScan create(ScanProcess scanProcess, Category category) {
        List<Optional<Scan>> list = category.getChannels().stream()
                .map(e -> Scan.create(scanProcess, e))
                .toList();
        return new CategoryScan(
                category,
                list.stream().filter(Optional::isPresent).map(Optional::get).toList());
    }

    @Override
    public List<Scan> scans() {
        return channels;
    }

    @Override
    public ScanProgress progress() {
        List<ScanProgress> list = channels.stream().map(Scan::progress).toList();
        return new ScanProgress(
                ScanTarget.CATEGORY, category.getIdLong(), category.getName(), scanned(), maxMessages(), hits(), list);
    }
}
