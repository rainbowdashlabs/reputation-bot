package de.chojo.repbot.analyzer.results.match;

import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import de.chojo.repbot.dao.snapshots.analyzer.match.AnswerResultSnapshot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class AnswerAnalyzerResult extends DirectAnalyzerResult {
    private final Message referenceMessage;
    private final long referenceMessageId;

    public AnswerAnalyzerResult(String match, Member donor, Member receiver, Message referenceMessage) {
        super(match, ThankType.ANSWER, donor, Collections.singletonList(receiver));
        this.referenceMessage = referenceMessage;
        referenceMessageId = referenceMessage.getIdLong();
    }

    public AnswerAnalyzerResult(String match, Member donor, Member receiver, long referenceMessageId) {
        super(match, ThankType.ANSWER, donor, Collections.singletonList(receiver));
        referenceMessage = null;
        this.referenceMessageId = referenceMessageId;
    }

    public long referenceMessageId() {
        return referenceMessageId;
    }

    @Nullable
    public Message referenceMessage() {
        return referenceMessage;
    }

    @Override
    public ResultSnapshot toSnapshot() {
        return new AnswerResultSnapshot(donor().getIdLong(), match(), receiverIds(), referenceMessageId);
    }
}
