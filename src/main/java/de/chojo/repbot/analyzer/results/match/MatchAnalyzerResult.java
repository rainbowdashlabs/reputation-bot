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

/**
 * Abstract class representing the result of a match analyzer.
 */
public abstract class MatchAnalyzerResult implements AnalyzerResult {
    private final ThankType thankType;
    private final Member donor;
    private final long donorId;
    private final String match;

    /**
     * Constructs a MatchAnalyzerResult instance.
     *
     * @param thankType the type of thank
     * @param donor     the member who gave the thank
     * @param match     the match string
     */
    public MatchAnalyzerResult(ThankType thankType, Member donor, String match) {
        this.thankType = thankType;
        this.donor = donor;
        donorId = donor == null ? 0 : donor.getIdLong();
        this.match = match;
    }

    /**
     * Returns the match string.
     *
     * @return the match string
     */
    public String match() {
        return match;
    }

    /**
     * Returns the list of members who received the thank.
     *
     * @return the list of members who received the thank
     */
    public abstract List<@Nullable Member> receivers();

    /**
     * Returns the member who gave the thank.
     *
     * @return the member who gave the thank, or null if not available
     */
    @Nullable
    public Member donor() {
        return donor;
    }

    /**
     * Returns the ID of the member who gave the thank.
     *
     * @return the ID of the member who gave the thank
     */
    public long donorId() {
        return donorId;
    }

    /**
     * Returns the type of thank.
     *
     * @return the type of thank
     */
    public ThankType thankType() {
        return thankType;
    }

    /**
     * Casts this result to an AnswerAnalyzerResult.
     *
     * @return this result as an AnswerAnalyzerResult
     */
    public AnswerAnalyzerResult asAnswer() {
        return (AnswerAnalyzerResult) this;
    }

    /**
     * Casts this result to a DirectAnalyzerResult.
     *
     * @return this result as a DirectAnalyzerResult
     */
    public DirectAnalyzerResult asMention() {
        return (DirectAnalyzerResult) this;
    }

    /**
     * Casts this result to a FuzzyAnalyzerResult.
     *
     * @return this result as a FuzzyAnalyzerResult
     */
    public FuzzyAnalyzerResult asFuzzy() {
        return (FuzzyAnalyzerResult) this;
    }

    /**
     * Returns the result type.
     *
     * @return the result type
     */
    @Override
    public ResultType resultType() {
        return ResultType.MATCH;
    }
}
