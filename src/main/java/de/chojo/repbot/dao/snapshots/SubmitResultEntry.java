/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots;

import de.chojo.repbot.service.reputation.SubmitResult;

import java.time.Instant;

public record SubmitResultEntry(SubmitResult submitResult, long channelId, long messageId, Instant instant) {
}
