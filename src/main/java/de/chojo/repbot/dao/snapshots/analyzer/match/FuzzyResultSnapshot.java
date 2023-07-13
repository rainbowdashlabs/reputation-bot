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
import de.chojo.repbot.analyzer.results.match.fuzzy.MemberMatch;
import de.chojo.repbot.dao.snapshots.ResultEntry;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class FuzzyResultSnapshot extends MatchResultSnapshot implements ResultSnapshot {
    private final List<String> thankwords;
    private final List<MemberMatch> memberMatches;

    @JsonCreator
    public FuzzyResultSnapshot(@JsonProperty("donor") long donor,
                               @JsonProperty("match") String match,
                               @JsonProperty("thankwords") List<String> thankwords,
                               @JsonProperty("memberMatches") List<MemberMatch> memberMatches) {
        super(ThankType.FUZZY, donor, match);
        this.thankwords = thankwords;
        this.memberMatches = memberMatches;
    }

    @Override
    public void add(Guild guild, ResultEntry entry, LocalizedEmbedBuilder builder) {
        super.add(guild, entry, builder);
        builder.addField("command.log.analyzer.message.field.matchedWords",
                       new HashSet<>(thankwords).stream()
                                                .map("`%s`"::formatted)
                                                .collect(Collectors.joining(", ")), false)
               .addField("command.log.analyzer.message.field.matchedUsers",
                       memberMatches.stream()
                                    .map(MemberMatch::asString)
                                    .collect(Collectors.joining("\n")), false);
    }
}
