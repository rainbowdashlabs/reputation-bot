/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.time.Instant;

class GeneralTest {
    private static final Instant NOW = Instant.parse("2026-10-23T12:00:00Z");
    private static final Instant PAST = NOW.minus(Duration.ofDays(1));

    @Test
    void noResetDate() {
        Assertions.assertNull(General.activeResetDate(ReputationMode.TOTAL, null, NOW));
    }

    @Test
    void pastResetDateApplies() {
        Assertions.assertEquals(PAST, General.activeResetDate(ReputationMode.TOTAL, PAST, NOW));
    }

    @Test
    void futureResetDateDoesNotApplyYet() {
        Assertions.assertNull(General.activeResetDate(ReputationMode.TOTAL, NOW.plus(Duration.ofDays(1)), NOW));
    }

    @ParameterizedTest
    @EnumSource(value = ReputationMode.class, names = "TOTAL", mode = EnumSource.Mode.EXCLUDE)
    void resetDateOnlyAppliesInTotalMode(ReputationMode mode) {
        Assertions.assertNull(General.activeResetDate(mode, PAST, NOW));
    }
}
