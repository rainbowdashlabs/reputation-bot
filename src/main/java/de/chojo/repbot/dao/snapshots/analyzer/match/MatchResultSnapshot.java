/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.analyzer.match;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.Colors;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.dao.snapshots.ResultEntry;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public abstract class MatchResultSnapshot implements ResultSnapshot {
    private final ThankType thankType;
    private final long donorId;
    private final String match;

    public MatchResultSnapshot(ThankType thankType, long donorId, String match) {
        this.thankType = thankType;
        this.donorId = donorId;
        this.match = match;
    }

    public ThankType thankType() {
        return thankType;
    }

    public long donorId() {
        return donorId;
    }

    public String match() {
        return match;
    }

    @Override
    public void add(Guild guild, ResultEntry entry, LocalizedEmbedBuilder builder) {
        builder.setTitle(thankType.nameLocaleKey())
               .setColor(Colors.Pastel.DARK_GREEN)
               .addField("command.log.analyzer.message.field.matchedWord", match, true)
               .addField("words.donor", User.fromId(donorId).getAsMention(), true);
    }
}
