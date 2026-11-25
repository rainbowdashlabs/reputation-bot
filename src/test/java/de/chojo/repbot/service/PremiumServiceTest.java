/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.dao.access.guild.subscriptions.SubscriptionError;
import de.chojo.repbot.util.SupporterFeature;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static de.chojo.repbot.util.SupporterFeature.AUTOPOST_USED;
import static de.chojo.repbot.util.SupporterFeature.CHANNEL_LIMIT_EXCEEDED;
import static de.chojo.repbot.util.SupporterFeature.LOG_CHANNEL_USED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PremiumServiceTest {
    private static final Instant SENT = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant AFTER_INTERVAL = SENT.plusSeconds(60);
    private static final Instant WITHIN_INTERVAL = SENT.minusSeconds(60);

    private static SubscriptionError reported(SupporterFeature feature) {
        return new SubscriptionError(feature, SENT, SENT, 1, true);
    }

    private static List<SupporterFeature> listed(
            Map<SupporterFeature, SubscriptionError> errors, Instant dueBefore, SupporterFeature... problems) {
        return PremiumService.listedInNotice(
                List.of(problems), feature -> errors.getOrDefault(feature, feature.first()), dueBefore);
    }

    @Test
    void reportedPausedFeatureIsLeftOutOfADueNotice() {
        var errors = Map.of(
                CHANNEL_LIMIT_EXCEEDED, reported(CHANNEL_LIMIT_EXCEEDED), AUTOPOST_USED, reported(AUTOPOST_USED));

        assertEquals(
                List.of(CHANNEL_LIMIT_EXCEEDED), listed(errors, AFTER_INTERVAL, CHANNEL_LIMIT_EXCEEDED, AUTOPOST_USED));
    }

    @Test
    void newPausedFeatureBringsBlockingFeaturesAlong() {
        var errors = Map.of(
                CHANNEL_LIMIT_EXCEEDED, reported(CHANNEL_LIMIT_EXCEEDED), AUTOPOST_USED, reported(AUTOPOST_USED));

        assertEquals(
                List.of(CHANNEL_LIMIT_EXCEEDED, LOG_CHANNEL_USED),
                listed(errors, WITHIN_INTERVAL, CHANNEL_LIMIT_EXCEEDED, AUTOPOST_USED, LOG_CHANNEL_USED));
    }

    @Test
    void nothingIsSentWhenNothingIsDue() {
        var errors = Map.of(
                CHANNEL_LIMIT_EXCEEDED, reported(CHANNEL_LIMIT_EXCEEDED), AUTOPOST_USED, reported(AUTOPOST_USED));

        assertEquals(List.of(), listed(errors, WITHIN_INTERVAL, CHANNEL_LIMIT_EXCEEDED, AUTOPOST_USED));
    }
}
