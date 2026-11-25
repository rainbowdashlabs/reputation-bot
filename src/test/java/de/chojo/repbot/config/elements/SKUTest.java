/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SKUTest {
    @Test
    void gracePeriodLastsOneIntervalPerAllowedNotice() {
        Instant reportedSince = Instant.parse("2026-01-01T00:00:00Z");

        Instant end = new SKU().gracePeriodEnd(reportedSince);

        assertEquals(Duration.ofHours(10L * 36), Duration.between(reportedSince, end));
    }
}
