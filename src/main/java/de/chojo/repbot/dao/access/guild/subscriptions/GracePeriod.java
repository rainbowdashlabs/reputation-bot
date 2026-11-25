/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.subscriptions;

import de.chojo.repbot.config.elements.SKU;
import de.chojo.repbot.util.SupporterFeature;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Decides when a guild that keeps using blocking supporter features stops receiving reputation.
 * <p>
 * The grace period starts with the earliest reported blocking feature that is still in use without entitlement and
 * lasts {@link SKU#gracePeriodEnd(Instant)}.
 */
public final class GracePeriod {
    private GracePeriod() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * The end of the grace period.
     *
     * @param errors      reported errors of the guild
     * @param stillInUse  whether the guild still uses a feature without entitlement
     * @param skus        supporter configuration
     * @return the end of the grace period, or empty if no blocking feature is still in use
     */
    public static Optional<Instant> end(
            Collection<SubscriptionError> errors, Predicate<SupporterFeature> stillInUse, SKU skus) {
        return errors.stream()
                .filter(error -> error.type().isBlocking())
                .filter(error -> stillInUse.test(error.type()))
                .map(SubscriptionError::created)
                .min(Comparator.naturalOrder())
                .map(skus::gracePeriodEnd);
    }

    /**
     * Whether the grace period is over.
     *
     * @param end end of the grace period, empty if there is none
     * @param now the current moment
     * @return true if the guild is blocked
     */
    public static boolean isOver(Optional<Instant> end, Instant now) {
        return end.map(at -> !at.isAfter(now)).orElse(false);
    }
}
