/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots;

import de.chojo.repbot.service.reputation.SubmitResult;

import java.time.Instant;

/**
 * Record representing an entry for a submit result.
 *
 * @param submitResult the result of the submission
 * @param channelId the ID of the channel where the submission occurred
 * @param messageId the ID of the message associated with the submission
 * @param instant the timestamp when the submission was made
 */
public record SubmitResultEntry(SubmitResult submitResult, long channelId, long messageId, Instant instant) {
}
