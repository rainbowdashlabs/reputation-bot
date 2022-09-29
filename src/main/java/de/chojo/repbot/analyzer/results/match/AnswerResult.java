package de.chojo.repbot.analyzer.results.match;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.Collections;

public class AnswerResult extends DirectResult {
    private final Message referenceMessage;

    public AnswerResult(Member donor, Member receiver, Message referenceMessage) {
        super(ThankType.ANSWER, donor, Collections.singletonList(receiver));
        this.referenceMessage = referenceMessage;
    }

    public Message referenceMessage() {
        return referenceMessage;
    }
}
