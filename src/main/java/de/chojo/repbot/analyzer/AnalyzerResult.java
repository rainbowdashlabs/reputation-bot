package de.chojo.repbot.analyzer;

import de.chojo.jdautil.parsing.WeightedEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record AnalyzerResult(ThankType type, Message referenceMessage, Member donator,
                             List<WeightedEntry<Member>> receivers) {
    private static final AnalyzerResult NO_MATCH = new AnalyzerResult(ThankType.NO_MATCH, null, null, null);
    private static final AnalyzerResult NO_TARGET = new AnalyzerResult(ThankType.NO_MATCH, null, null, null);

    public AnalyzerResult(ThankType type, Message referenceMessage, Member donator, List<WeightedEntry<Member>> receivers) {
        this.type = type;
        this.referenceMessage = referenceMessage;
        this.donator = donator;
        this.receivers = receivers == null ? Collections.emptyList() : receivers;
    }

    public static AnalyzerResult noMatch() {
        return NO_MATCH;
    }

    public static AnalyzerResult noTarget(Member donator) {
        return new AnalyzerResult(ThankType.NO_TARGET, null, donator, null);
    }

    public static AnalyzerResult mention(Member donator, List<Member> receiver) {
        return new AnalyzerResult(ThankType.MENTION, null, donator, receiver.stream()
                                                                            .map(u -> WeightedEntry.withWeight(u, 1))
                                                                            .collect(Collectors.toList()));
    }

    public static AnalyzerResult answer(Member donator, Member receiver, Message referenceMessage) {
        return new AnalyzerResult(ThankType.ANSWER, referenceMessage, donator, Collections.singletonList(WeightedEntry.withWeight(receiver, 1)));
    }

    public static AnalyzerResult fuzzy(Member donator, List<WeightedEntry<Member>> receivers) {
        return new AnalyzerResult(ThankType.FUZZY, null, donator, receivers);
    }

    public boolean isEmpty() {
        return receivers.isEmpty();
    }
}
