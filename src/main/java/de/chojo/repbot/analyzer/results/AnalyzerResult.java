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

public interface AnalyzerResult {
    static AnalyzerResult empty(EmptyResultReason resultReason) {
        return new EmptyAnalyzerResult(null, resultReason);
    }

    static AnalyzerResult empty(String match, EmptyResultReason resultReason) {
        return new EmptyAnalyzerResult(match, resultReason);
    }

    static AnalyzerResult mention(String match, Member donator, List<Member> receiver) {
        return new DirectAnalyzerResult(match, ThankType.MENTION, donator, receiver);
    }

    static AnalyzerResult answer(String match, Member donator, Member receiver, Message referenceMessage) {
        return new AnswerAnalyzerResult(match, donator, receiver, referenceMessage.getIdLong());
    }

    static AnalyzerResult fuzzy(
            String match,
            List<String> thankwords,
            List<MemberMatch> memberMatches,
            Member donator,
            List<WeightedEntry<Member>> receivers) {
        return new FuzzyAnalyzerResult(match, thankwords, memberMatches, donator, receivers);
    }

    ResultType resultType();

    default EmptyAnalyzerResult asEmpty() {
        return (EmptyAnalyzerResult) this;
    }

    default MatchAnalyzerResult asMatch() {
        return (MatchAnalyzerResult) this;
    }

    default boolean isEmpty() {
        return resultType() == ResultType.NO_MATCH;
    }

    ResultSnapshot toSnapshot();
}
