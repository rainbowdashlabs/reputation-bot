/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.subscriptions;

import de.chojo.repbot.util.SupporterFeature;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionErrorTest {
    private static final Instant SENT = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant AFTER_INTERVAL = SENT.plusSeconds(60);
    private static final Instant WITHIN_INTERVAL = SENT.minusSeconds(60);

    private static SubscriptionError sent(SupporterFeature feature, boolean notified) {
        return new SubscriptionError(feature, SENT, SENT, 1, notified);
    }

    @Test
    void unreportedFeaturesAreDue() {
        assertTrue(SupporterFeature.AUTOPOST_USED.first().isDue(SENT));
        assertTrue(SupporterFeature.CHANNEL_LIMIT_EXCEEDED.first().isDue(SENT));
    }

    @Test
    void blockingFeaturesRepeatEveryInterval() {
        var error = sent(SupporterFeature.CHANNEL_LIMIT_EXCEEDED, true);

        assertFalse(error.isDue(WITHIN_INTERVAL));
        assertTrue(error.isDue(AFTER_INTERVAL));
    }

    @Test
    void pausedFeaturesAreReportedOnce() {
        assertFalse(sent(SupporterFeature.AUTOPOST_USED, true).isDue(AFTER_INTERVAL));
    }

    @Test
    void pausedFeaturesRepeatWhenNobodyWasReached() {
        var error = sent(SupporterFeature.AUTOPOST_USED, false);

        assertFalse(error.isDue(WITHIN_INTERVAL));
        assertTrue(error.isDue(AFTER_INTERVAL));
    }
}
