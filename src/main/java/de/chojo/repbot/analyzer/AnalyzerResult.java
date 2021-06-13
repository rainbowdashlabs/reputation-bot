package de.chojo.repbot.analyzer;

import de.chojo.jdautil.parsing.WeightedEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyzerResult {
    private static final AnalyzerResult NO_MATCH = new AnalyzerResult(ThankType.NO_MATCH, null, null, null);
    private static final AnalyzerResult NO_TARGET = new AnalyzerResult(ThankType.NO_MATCH, null, null, null);
    private final ThankType type;
    private final Message referenceMessage;
    private final User donator;
    private final List<WeightedEntry<Member>> receivers;

    public AnalyzerResult(ThankType type, Message referenceMessage, User donator, List<WeightedEntry<Member>> receivers) {
        this.type = type;
        this.referenceMessage = referenceMessage;
        this.donator = donator;
        this.receivers = receivers == null ? Collections.emptyList() : receivers;
    }

    public static AnalyzerResult noMatch() {
        return NO_MATCH;
    }

    public static AnalyzerResult noTarget(User donator) {
        return new AnalyzerResult(ThankType.NO_TARGET, null, donator, null);
    }

    public static AnalyzerResult mention(User donator, List<Member> receiver) {
        return new AnalyzerResult(ThankType.MENTION, null, donator, receiver.stream().map(u -> WeightedEntry.withWeight(u, 1)).collect(Collectors.toList()));
    }

    public static AnalyzerResult answer(User donator, Member receiver, Message referenceMessage) {
        return new AnalyzerResult(ThankType.ANSWER, referenceMessage, donator, Collections.singletonList(WeightedEntry.withWeight(receiver, 1)));
    }

    public static AnalyzerResult fuzzy(User donator, List<WeightedEntry<Member>> receivers) {
        return new AnalyzerResult(ThankType.FUZZY, null, donator, receivers);
    }

    public boolean isEmpty() {
        return receivers.isEmpty();
    }

    public ThankType type() {
        return type;
    }

    public Message referenceMessage() {
        return referenceMessage;
    }

    public User donator() {
        return donator;
    }

    public List<WeightedEntry<Member>> receivers() {
        return receivers;
    }
}
