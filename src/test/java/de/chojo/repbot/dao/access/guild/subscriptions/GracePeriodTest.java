/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.subscriptions;

import de.chojo.repbot.config.elements.SKU;
import de.chojo.repbot.util.SupporterFeature;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GracePeriodTest {
    private static final SKU SKUS = new SKU();
    private static final Instant EARLY = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant LATE = Instant.parse("2026-01-05T00:00:00Z");

    private static SubscriptionError error(SupporterFeature feature, Instant created) {
        return new SubscriptionError(feature, created, created, 1, true);
    }

    @Test
    void earliestBlockingErrorStartsThePeriod() {
        var errors = List.of(
                error(SupporterFeature.CATEGORY_LIMIT_EXCEEDED, LATE),
                error(SupporterFeature.CHANNEL_LIMIT_EXCEEDED, EARLY));

        assertEquals(Optional.of(SKUS.gracePeriodEnd(EARLY)), GracePeriod.end(errors, feature -> true, SKUS));
    }

    @Test
    void nonBlockingErrorsDoNotStartAPeriod() {
        var errors = List.of(error(SupporterFeature.AUTOPOST_USED, EARLY));

        assertEquals(Optional.empty(), GracePeriod.end(errors, feature -> true, SKUS));
    }

    @Test
    void errorsNoLongerInUseAreIgnored() {
        var errors = List.of(
                error(SupporterFeature.CHANNEL_LIMIT_EXCEEDED, EARLY), error(SupporterFeature.BOT_NICKNAMED, LATE));

        var end = GracePeriod.end(errors, feature -> feature != SupporterFeature.CHANNEL_LIMIT_EXCEEDED, SKUS);

        assertEquals(Optional.of(SKUS.gracePeriodEnd(LATE)), end);
    }

    @Test
    void blockedOnceTheEndIsReached() {
        Instant end = SKUS.gracePeriodEnd(EARLY);

        assertFalse(GracePeriod.isOver(Optional.of(end), end.minusSeconds(1)));
        assertTrue(GracePeriod.isOver(Optional.of(end), end));
        assertFalse(GracePeriod.isOver(Optional.empty(), end));
    }
}
