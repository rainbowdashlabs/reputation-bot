/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.analyzer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.repbot.dao.snapshots.ResultEntry;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Interface for result snapshots.
 * This interface is used to add result entries to a snapshot with a localized embed builder.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface ResultSnapshot {
    /**
     * Adds a result entry to the snapshot.
     *
     * @param guild the guild where the result entry is being added
     * @param entry the result entry to add
     * @param builder the localized embed builder to use
     */
    void add(Guild guild, ResultEntry entry, LocalizedEmbedBuilder builder);
}
