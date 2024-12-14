/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results.match;

import de.chojo.jdautil.parsing.WeightedEntry;
import de.chojo.repbot.analyzer.results.match.fuzzy.MemberMatch;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import de.chojo.repbot.dao.snapshots.analyzer.match.FuzzyResultSnapshot;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the result of a fuzzy match analysis.
 */
public class FuzzyAnalyzerResult extends MatchAnalyzerResult {
    private final List<String> thankwords;
    private final List<MemberMatch> memberMatches;
    private final List<WeightedEntry<Member>> weightedReceiver;

    /**
     * Constructs a new FuzzyAnalyzerResult.
     *
     * @param match            the match string
     * @param thankwords       the list of thank words
     * @param memberMatches    the list of member matches
     * @param donor            the donor member
     * @param weightedReceiver the list of weighted receiver entries
     */
    public FuzzyAnalyzerResult(String match, List<String> thankwords, List<MemberMatch> memberMatches, Member donor, List<WeightedEntry<Member>> weightedReceiver) {
        super(ThankType.FUZZY, donor, match);
        this.thankwords = thankwords;
        this.memberMatches = memberMatches;
        this.weightedReceiver = weightedReceiver;
    }

    /**
     * Retrieves the list of receiver members.
     *
     * @return the list of receiver members
     */
    @Override
    public List<Member> receivers() {
        return weightedReceiver.stream().map(WeightedEntry::getReference).collect(Collectors.toList());
    }

    /**
     * Retrieves the list of weighted receiver entries.
     *
     * @return the list of weighted receiver entries
     */
    public List<WeightedEntry<Member>> weightedReceiver() {
        return weightedReceiver;
    }

    /**
     * Retrieves the list of member matches.
     *
     * @return the list of member matches
     */
    public List<MemberMatch> memberMatches() {
        return memberMatches;
    }

    /**
     * Retrieves the list of thank words.
     *
     * @return the list of thank words
     */
    public List<String> thankwords() {
        return thankwords;
    }

    /**
     * Converts the result to a snapshot.
     *
     * @return the result snapshot
     */
    @Override
    public ResultSnapshot toSnapshot() {
        return new FuzzyResultSnapshot(donorId(), match(), thankwords, memberMatches);
    }
}
