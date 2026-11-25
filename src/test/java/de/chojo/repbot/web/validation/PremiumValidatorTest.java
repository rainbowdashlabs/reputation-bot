/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.validation;

import de.chojo.repbot.util.States;
import de.chojo.repbot.web.error.PremiumFeatureException;
import de.chojo.repbot.web.pojo.premium.FeatureLimit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class PremiumValidatorTest {
    private static final FeatureLimit LOCKED = new FeatureLimit(3, false, List.of());
    private static final FeatureLimit UNLOCKED = new FeatureLimit(3, true, List.of());

    private final PremiumValidator validator = new PremiumValidator(null);

    @Test
    void acceptsCountWithinLimit() {
        assertDoesNotThrow(() -> validator.requireWithinLimitOrReduced(3, 0, LOCKED, "Test"));
    }

    @Test
    void refusesGrowingAboveLimit() {
        assumeFalse(States.GRANT_ALL_SKU);
        assertThrows(PremiumFeatureException.class, () -> validator.requireWithinLimitOrReduced(5, 4, LOCKED, "Test"));
    }

    @Test
    void acceptsSameCountAboveLimit() {
        assertDoesNotThrow(() -> validator.requireWithinLimitOrReduced(5, 5, LOCKED, "Test"));
    }

    @Test
    void acceptsReducingAboveLimit() {
        assertDoesNotThrow(() -> validator.requireWithinLimitOrReduced(4, 5, LOCKED, "Test"));
    }

    @Test
    void acceptsAnyCountWhenUnlocked() {
        assertDoesNotThrow(() -> validator.requireWithinLimitOrReduced(10, 0, UNLOCKED, "Test"));
    }
}
