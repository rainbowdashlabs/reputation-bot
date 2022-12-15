package de.chojo.repbot.dao.snapshots;

import de.chojo.repbot.service.reputation.SubmitResult;

import java.time.Instant;

public record SubmitResultEntry(SubmitResult submitResult, long channelId, long messageId, Instant instant) {
}
