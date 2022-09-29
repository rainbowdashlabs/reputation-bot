package de.chojo.repbot.analyzer.results.match;

import de.chojo.repbot.analyzer.results.Result;
import de.chojo.repbot.analyzer.results.ResultType;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public abstract class MatchResult implements Result {
    private final ThankType thankType;
    private final Member donor;

    public MatchResult(ThankType thankType, Member donor) {
        this.thankType = thankType;
        this.donor = donor;
    }

    public abstract List<Member> receivers();

    public Member donor() {
        return donor;
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
