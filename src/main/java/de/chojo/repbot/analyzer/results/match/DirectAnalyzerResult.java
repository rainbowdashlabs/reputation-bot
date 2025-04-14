/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results.match;

import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import de.chojo.repbot.dao.snapshots.analyzer.match.DirectResultSnapshot;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

/**
 * Class representing the result of a direct analyzer match.
 */
public class DirectAnalyzerResult extends MatchAnalyzerResult {
    private final List<Member> receivers;

    /**
     * Constructs a DirectAnalyzerResult instance.
     *
     * @param match     the match string
     * @param type      the type of thank
     * @param donor     the member who gave the thank
     * @param receivers the list of members who received the thank
     */
    public DirectAnalyzerResult(String match, ThankType type, Member donor, List<Member> receivers) {
        super(type, donor, match);
        this.receivers = receivers;
    }

    /**
     * Returns the list of receiver IDs.
     *
     * @return the list of receiver IDs
     */
    protected List<Long> receiverIds() {
        return receivers.stream().map(Member::getIdLong).toList();
    }

    /**
     * Returns the list of members who received the thank.
     *
     * @return the list of members who received the thank
     */
    @Override
    public List<Member> receivers() {
        return receivers;
    }

    /**
     * Converts this result to a snapshot.
     *
     * @return the result snapshot
     */
    @Override
    public ResultSnapshot toSnapshot() {
        return new DirectResultSnapshot(thankType(), donorId(), match(), receiverIds());
    }
}
