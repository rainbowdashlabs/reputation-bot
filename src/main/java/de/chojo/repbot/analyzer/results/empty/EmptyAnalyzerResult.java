/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results.empty;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.repbot.analyzer.results.AnalyzerResult;
import de.chojo.repbot.analyzer.results.ResultType;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import de.chojo.repbot.dao.snapshots.ResultEntry;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;

/**
 * Represents an empty analyzer result with a match and a reason.
 *
 * @param match the matched string, can be null
 * @param reason the reason for the empty result
 */
public record EmptyAnalyzerResult(@Nullable String match, EmptyResultReason reason) implements AnalyzerResult, ResultSnapshot {

    /**
     * Returns the result type of this analyzer result.
     *
     * @return the result type, which is always {@link ResultType#NO_MATCH}
     */
    @Override
    public ResultType resultType() {
        return ResultType.NO_MATCH;
    }

    /**
     * Converts this analyzer result to a snapshot.
     *
     * @return this analyzer result as a snapshot
     */
    @Override
    public ResultSnapshot toSnapshot() {
        return this;
    }

    /**
     * Adds this analyzer result to the provided embed builder.
     *
     * @param guild the guild where the result was generated
     * @param entry the result entry
     * @param builder the localized embed builder to add the result to
     */
    @Override
    public void add(Guild guild, ResultEntry entry, LocalizedEmbedBuilder builder) {
        builder.setTitle(reason.localeKey())
                .setColor(Colors.Pastel.ORANGE);
        if (match != null) {
            builder.addField("command.log.analyzer.message.field.matchedWord", match, false);
        }
    }
}
