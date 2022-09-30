package de.chojo.repbot.analyzer.results.match;

import de.chojo.repbot.analyzer.results.Result;
import de.chojo.repbot.analyzer.results.ResultType;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MatchResult implements Result {
    private final ThankType thankType;
    private final Member donor;
    private final long donorId;
    private final String match;

    public MatchResult(ThankType thankType, Member donor, String match) {
        this.thankType = thankType;
        this.donor = donor;
        donorId = donor.getIdLong();
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

    public AnswerResult asAnswer() {
        return (AnswerResult) this;
    }

    public DirectResult asMention() {
        return (DirectResult) this;
    }

    public FuzzyResult asFuzzy() {
        return (FuzzyResult) this;
    }

    @Override
    public ResultType resultType() {
        return ResultType.MATCH;
    }
}
