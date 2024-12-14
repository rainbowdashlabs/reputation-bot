/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results.match;

import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import de.chojo.repbot.dao.snapshots.analyzer.match.AnswerResultSnapshot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * Represents the result of an answer analyzer.
 */
public class AnswerAnalyzerResult extends DirectAnalyzerResult {
    private final Message referenceMessage;
    private final long referenceMessageId;

    /**
     * Constructs an AnswerAnalyzerResult instance.
     *
     * @param match            the match string
     * @param donor            the member who gave the thank
     * @param receiver         the member who received the thank
     * @param referenceMessage the reference message
     */
    public AnswerAnalyzerResult(String match, Member donor, Member receiver, Message referenceMessage) {
        super(match, ThankType.ANSWER, donor, Collections.singletonList(receiver));
        this.referenceMessage = referenceMessage;
        referenceMessageId = referenceMessage.getIdLong();
    }

    /**
     * Constructs an AnswerAnalyzerResult instance with a reference message ID.
     *
     * @param match              the match string
     * @param donor              the member who gave the thank
     * @param receiver           the member who received the thank
     * @param referenceMessageId the ID of the reference message
     */
    public AnswerAnalyzerResult(String match, Member donor, Member receiver, long referenceMessageId) {
        super(match, ThankType.ANSWER, donor, Collections.singletonList(receiver));
        referenceMessage = null;
        this.referenceMessageId = referenceMessageId;
    }

    /**
     * Returns the ID of the reference message.
     *
     * @return the ID of the reference message
     */
    public long referenceMessageId() {
        return referenceMessageId;
    }

    /**
     * Returns the reference message.
     *
     * @return the reference message, or null if not available
     */
    @Nullable
    public Message referenceMessage() {
        return referenceMessage;
    }

    /**
     * Converts this result to a snapshot.
     *
     * @return the result snapshot
     */
    @Override
    public ResultSnapshot toSnapshot() {
        return new AnswerResultSnapshot(donorId(), match(), receiverIds(), referenceMessageId);
    }
}
