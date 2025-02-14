/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results;

import de.chojo.jdautil.parsing.WeightedEntry;
import de.chojo.repbot.analyzer.results.empty.EmptyAnalyzerResult;
import de.chojo.repbot.analyzer.results.empty.EmptyResultReason;
import de.chojo.repbot.analyzer.results.match.AnswerAnalyzerResult;
import de.chojo.repbot.analyzer.results.match.DirectAnalyzerResult;
import de.chojo.repbot.analyzer.results.match.FuzzyAnalyzerResult;
import de.chojo.repbot.analyzer.results.match.MatchAnalyzerResult;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.analyzer.results.match.fuzzy.MemberMatch;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * Interface representing the result of an analyzer.
 */
public interface AnalyzerResult {
    /**
     * Returns the result type.
     *
     * @return the result type
     */
    ResultType resultType();

    /**
     * Casts this result to an EmptyAnalyzerResult.
     *
     * @return this result as an EmptyAnalyzerResult
     */
    default EmptyAnalyzerResult asEmpty() {
        return (EmptyAnalyzerResult) this;
    }

    /**
     * Casts this result to a MatchAnalyzerResult.
     *
     * @return this result as a MatchAnalyzerResult
     */
    default MatchAnalyzerResult asMatch() {
        return (MatchAnalyzerResult) this;
    }

    /**
     * Creates an empty analyzer result with the specified reason.
     *
     * @param resultReason the reason for the empty result
     * @return the empty analyzer result
     */
    static AnalyzerResult empty(EmptyResultReason resultReason) {
        return new EmptyAnalyzerResult(null, resultReason);
    }

    /**
     * Creates an empty analyzer result with the specified match string and reason.
     *
     * @param match        the match string
     * @param resultReason the reason for the empty result
     * @return the empty analyzer result
     */
    static AnalyzerResult empty(String match, EmptyResultReason resultReason) {
        return new EmptyAnalyzerResult(match, resultReason);
    }

    /**
     * Creates a direct analyzer result with the specified match string, donor, and receivers.
     *
     * @param match    the match string
     * @param donator  the member who gave the thank
     * @param receiver the list of members who received the thank
     * @return the direct analyzer result
     */
    static AnalyzerResult mention(String match, Member donator, List<Member> receiver) {
        return new DirectAnalyzerResult(match, ThankType.MENTION, donator, receiver);
    }

    /**
     * Creates an answer analyzer result with the specified match string, donor, receiver, and reference message.
     *
     * @param match            the match string
     * @param donator          the member who gave the thank
     * @param receiver         the member who received the thank
     * @param referenceMessage the reference message
     * @return the answer analyzer result
     */
    static AnalyzerResult answer(String match, Member donator, Member receiver, Message referenceMessage) {
        return new AnswerAnalyzerResult(match, donator, receiver, referenceMessage.getIdLong());
    }

    /**
     * Creates a fuzzy analyzer result with the specified match string, thank words, member matches, donor, and receivers.
     *
     * @param match         the match string
     * @param thankwords    the list of thank words
     * @param memberMatches the list of member matches
     * @param donator       the member who gave the thank
     * @param receivers     the list of weighted entries of members who received the thank
     * @return the fuzzy analyzer result
     */
    static AnalyzerResult fuzzy(String match, List<String> thankwords, List<MemberMatch> memberMatches, Member donator, List<WeightedEntry<Member>> receivers) {
        return new FuzzyAnalyzerResult(match, thankwords, memberMatches, donator, receivers);
    }

    /**
     * Checks if the result is empty.
     *
     * @return true if the result is empty, false otherwise
     */
    default boolean isEmpty() {
        return resultType() == ResultType.NO_MATCH;
    }

    /**
     * Converts this result to a snapshot.
     *
     * @return the result snapshot
     */
    ResultSnapshot toSnapshot();
}
