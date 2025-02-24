/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.analyzer.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.dao.snapshots.ResultEntry;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a snapshot of a direct result match.
 */
public class DirectResultSnapshot extends MatchResultSnapshot implements ResultSnapshot {
    private final List<Long> receivers;

    /**
     * Constructs a new DirectResultSnapshot.
     *
     * @param thankType the type of thank
     * @param donorId   the ID of the donor
     * @param match     the match string
     * @param receivers the list of receiver IDs
     */
    @JsonCreator
    public DirectResultSnapshot(@JsonProperty("thankType") ThankType thankType,
                                @JsonProperty("donorId") long donorId,
                                @JsonProperty("match") String match,
                                @JsonProperty("receivers") List<Long> receivers) {
        super(thankType, donorId, match);
        this.receivers = receivers;
    }

    /**
     * Retrieves the list of receiver IDs.
     *
     * @return the list of receiver IDs
     */
    public List<Long> receivers() {
        return receivers;
    }

    /**
     * Adds the result entry to the localized embed builder.
     *
     * @param guild   the guild
     * @param entry   the result entry
     * @param builder the localized embed builder
     */
    @Override
    public void add(Guild guild, ResultEntry entry, LocalizedEmbedBuilder builder) {
        super.add(guild, entry, builder);
        builder.addField("command.log.analyzer.message.field.receivers",
                receivers().stream()
                           .map(id -> User.fromId(id).getAsMention())
                           .collect(Collectors.joining("\n")), false);
    }
}
