/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.premium;

import de.chojo.repbot.commands.settings.DashboardSection;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.util.SupporterFeature;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static de.chojo.repbot.util.States.GRANT_ALL_SKU;

/**
 * The supporter features a guild uses without entitlement and whether it is blocked because of them.
 *
 * @param issues   features in use without entitlement
 * @param blocksAt ISO-8601 instant when the guild stops receiving reputation, or null if no blocking feature was
 *                 reported
 * @param blocked  whether the guild no longer receives reputation
 */
public record SupporterStatusPOJO(List<SupporterIssuePOJO> issues, String blocksAt, boolean blocked) {

    /**
     * Builds the current status of the guild from its settings and reported errors.
     *
     * @param guild guild to inspect
     * @return status of the guild
     */
    public static SupporterStatusPOJO of(RepGuild guild) {
        if (GRANT_ALL_SKU) return new SupporterStatusPOJO(List.of(), null, false);
        var skus = guild.configuration().skus();
        List<SupporterIssuePOJO> issues = Arrays.stream(SupporterFeature.values())
                .filter(feature -> feature.isUnentitledUse(skus, guild))
                .map(feature -> new SupporterIssuePOJO(
                        feature.name(),
                        feature.dashboardSection().map(DashboardSection::path).orElse(null),
                        feature.isBlocking()))
                .toList();
        boolean blockingInUse = issues.stream().anyMatch(SupporterIssuePOJO::blocking);
        if (!blockingInUse) return new SupporterStatusPOJO(issues, null, false);
        var subscriptions = guild.subscriptions();
        return new SupporterStatusPOJO(
                issues, subscriptions.blocksAt().map(Instant::toString).orElse(null), subscriptions.isBlocked());
    }
}
