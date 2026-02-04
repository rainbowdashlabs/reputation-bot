/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results.empty;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.repbot.analyzer.results.AnalyzerResult;
import de.chojo.repbot.analyzer.results.ResultType;
import de.chojo.repbot.dao.snapshots.ResultEntry;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;

public record EmptyAnalyzerResult(@Nullable String match,
                                  EmptyResultReason reason) implements AnalyzerResult, ResultSnapshot {

    @Override
    public ResultType resultType() {
        return ResultType.NO_MATCH;
    }

    @Override
    public ResultSnapshot toSnapshot() {
        return this;
    }

    @Override
    public void add(Guild guild, ResultEntry entry, LocalizedEmbedBuilder builder) {
        builder.setTitle(reason.localeKey())
               .setColor(Colors.Pastel.ORANGE);
        if (match != null) {
            builder.addField("command.log.analyzer.message.field.matchedWord", match, false);
        }
    }
}
