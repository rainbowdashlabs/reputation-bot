package de.chojo.repbot.analyzer;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

@Getter
public class AnalyzerResult {
    private static final AnalyzerResult NO_MATCH = new AnalyzerResult(ResultType.NO_MATCH, null, null, null, 1);

    ResultType type;
    Message referenceMessage;
    User donator;
    User receiver;
    double confidenceScore;

    public AnalyzerResult(ResultType type, Message referenceMessage, User donator, User receiver, double confidenceScore) {
        this.type = type;
        this.referenceMessage = referenceMessage;
        this.donator = donator;
        this.receiver = receiver;
        this.confidenceScore = confidenceScore;
    }

    public static AnalyzerResult noMatch() {
        return NO_MATCH;
    }

    public static AnalyzerResult mention(User donator, User receiver) {
        return new AnalyzerResult(ResultType.MENTION, null, donator, receiver, 1);
    }

    public static AnalyzerResult answer(User donator, User receiver, Message referenceMessage) {
        return new AnalyzerResult(ResultType.ANSWER, referenceMessage, donator, receiver, 1);
    }

    public static AnalyzerResult fuzzy(User donator, User receiver, double confidenceScore) {
        return new AnalyzerResult(ResultType.FUZZY, null, donator, receiver, confidenceScore);
    }
}
