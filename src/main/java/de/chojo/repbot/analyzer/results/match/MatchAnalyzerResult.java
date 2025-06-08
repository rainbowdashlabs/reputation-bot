/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results.match;

import de.chojo.repbot.analyzer.results.AnalyzerResult;
import de.chojo.repbot.analyzer.results.ResultType;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MatchAnalyzerResult implements AnalyzerResult {
    private final ThankType thankType;
    private final Member donor;
    private final long donorId;
    private final String match;

    public MatchAnalyzerResult(ThankType thankType, Member donor, String match) {
        this.thankType = thankType;
        this.donor = donor;
        donorId = donor == null ? null : donor.getIdLong();
        this.match = match;
    }

    public String match() {
        return match;
    }

    public abstract List<@Nullable Member> receivers();

    @Nullable
    public Member donor() {
        return donor;
    }

    public long donorId() {
        return donorId;
    }

    public ThankType thankType() {
        return thankType;
    }

    public AnswerAnalyzerResult asAnswer() {
        return (AnswerAnalyzerResult) this;
    }

    public DirectAnalyzerResult asMention() {
        return (DirectAnalyzerResult) this;
    }

    public FuzzyAnalyzerResult asFuzzy() {
        return (FuzzyAnalyzerResult) this;
    }

    @Override
    public ResultType resultType() {
        return ResultType.MATCH;
    }
}
