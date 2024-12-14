/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.analyzer.match;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.Colors;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import de.chojo.repbot.dao.snapshots.ResultEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

/**
 * Abstract class representing a snapshot of a match result.
 */
public abstract class MatchResultSnapshot implements ResultSnapshot {
    private final ThankType thankType;
    private final long donorId;
    private final String match;

    /**
     * Constructs a new MatchResultSnapshot with the specified thank type, donor ID, and match.
     *
     * @param thankType the type of thank
     * @param donorId the ID of the donor
     * @param match the matched string
     */
    public MatchResultSnapshot(ThankType thankType, long donorId, String match) {
        this.thankType = thankType;
        this.donorId = donorId;
        this.match = match;
    }

    /**
     * Returns the type of thank.
     *
     * @return the thank type
     */
    public ThankType thankType() {
        return thankType;
    }

    /**
     * Returns the ID of the donor.
     *
     * @return the donor ID
     */
    public long donorId() {
        return donorId;
    }

    /**
     * Returns the matched string.
     *
     * @return the match
     */
    public String match() {
        return match;
    }

    /**
     * Adds the match result information to the provided LocalizedEmbedBuilder.
     *
     * @param guild the guild
     * @param entry the result entry
     * @param builder the LocalizedEmbedBuilder to add information to
     */
    @Override
    public void add(Guild guild, ResultEntry entry, LocalizedEmbedBuilder builder) {
        builder.setTitle(thankType.nameLocaleKey())
                .setColor(Colors.Pastel.DARK_GREEN)
                .addField("command.log.analyzer.message.field.matchedWord", match, true)
                .addField("words.donor", User.fromId(donorId).getAsMention(), true);
    }
}
