/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.statistic.element;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.statistic.ReplacementProvider;
import net.dv8tion.jda.api.JDA;

import java.util.List;

public record ShardStatistic(int shard, JDA.Status status, long analyzedMessages, long guilds)
        implements ReplacementProvider {

    @Override
    public List<Replacement> replacements() {
        return List.of(
                Replacement.create("analyzed_messages_shard", analyzedMessages),
                Replacement.create("shard_status", status.name()),
                Replacement.create("shard_id", shard),
                Replacement.create("shard_guilds", guilds));
    }
}
