/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

class GeneralTest {
    private static final Instant NOW = Instant.parse("2026-10-23T12:00:00Z");

    @Test
    void noResetDate() {
        Assertions.assertNull(General.activeResetDate(null, NOW));
    }

    @Test
    void pastResetDateApplies() {
        var past = NOW.minus(Duration.ofDays(1));
        Assertions.assertEquals(past, General.activeResetDate(past, NOW));
    }

    @Test
    void futureResetDateDoesNotApplyYet() {
        Assertions.assertNull(General.activeResetDate(NOW.plus(Duration.ofDays(1)), NOW));
    }
}
