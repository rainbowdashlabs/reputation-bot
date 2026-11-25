/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.commands.settings.DashboardSection;
import de.chojo.repbot.config.elements.SKU;
import de.chojo.repbot.config.elements.sku.SKUEntry;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.subscriptions.SubscriptionError;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static de.chojo.jdautil.localization.util.Replacement.create;

/**
 * A supporter feature a guild can use while it is not entitled to it.
 * <p>
 * Blocking features get a grace period: the guild is warned repeatedly and stops receiving reputation once the
 * warnings reach the configured threshold. Non-blocking features are paused as soon as the entitlement ends and are
 * only reported.
 */
public enum SupporterFeature {
    CHANNEL_LIMIT_EXCEEDED(
            (sku, repGuild) -> List.of(
                    create("ALLOWED", sku.features().reputationChannel().defaultChannel()),
                    create(
                            "COUNT",
                            repGuild.settings()
                                    .thanking()
                                    .channels()
                                    .accessibleChannels()
                                    .size())),
            (sku, repGuild) -> repGuild.settings()
                            .thanking()
                            .channels()
                            .accessibleChannels()
                            .size()
                    > sku.features().reputationChannel().defaultChannel(),
            sku -> sku.features().reputationChannel().fullSkuEntry(),
            DashboardSection.CHANNELS,
            true),
    CATEGORY_LIMIT_EXCEEDED(
            (sku, repGuild) -> List.of(
                    create("ALLOWED", sku.features().reputationCategories().defaultCategories()),
                    create(
                            "COUNT",
                            repGuild.settings()
                                    .thanking()
                                    .channels()
                                    .categories()
                                    .size())),
            (sku, repGuild) ->
                    repGuild.settings().thanking().channels().categories().size()
                            > sku.features().reputationCategories().defaultCategories(),
            sku -> sku.features().reputationCategories().fullSkuEntry(),
            DashboardSection.CHANNELS,
            true),
    BLACKLIST_USED(
            settings -> !settings.thanking().channels().isWhitelist(),
            sku -> sku.features().channelBlacklist().fullSkuEntry(),
            DashboardSection.CHANNELS,
            true),
    BOT_NICKNAMED(
            settings -> settings.guild().getSelfMember().getNickname() != null,
            sku -> sku.features().profile().fullSkuEntry(),
            null,
            true),
    AUTOPOST_USED(
            settings -> settings.autopost().active(),
            sku -> sku.features().autopost().fullSkuEntry(),
            DashboardSection.AUTOPOST,
            false),
    LOG_CHANNEL_USED(
            settings -> settings.logChannel().active(),
            sku -> sku.features().logChannel().fullSkuEntry(),
            DashboardSection.LOG_CHANNEL,
            false),
    COMMAND_ALIASES_USED(
            settings -> !settings.commandAliases().aliases().isEmpty(),
            sku -> sku.features().commandAliases().fullSkuEntry(),
            DashboardSection.COMMAND_ALIASES,
            false),
    INTEGRATION_BYPASS_USED(
            settings -> settings.integrationBypass().hasBypasses(),
            sku -> sku.features().integrationBypass().fullSkuEntry(),
            DashboardSection.INTEGRATION_BYPASS,
            false),
    ADDITIONAL_EMOJIS_USED(
            settings -> !settings.thanking().reactions().reactions().isEmpty(),
            sku -> sku.features().additionalEmojis().fullSkuEntry(),
            DashboardSection.REACTIONS,
            false),
    LOCALE_OVERRIDES_USED(
            settings -> settings.profile().reputationName() != null,
            sku -> sku.features().localeOverrides().fullSkuEntry(),
            DashboardSection.PROFILE,
            false);

    private final BiFunction<SKU, RepGuild, List<Replacement>> replacements;
    private final BiFunction<SKU, RepGuild, Boolean> isApplicable;
    private final Function<SKU, SKUEntry> skus;
    private final DashboardSection dashboardSection;
    private final boolean blocking;

    SupporterFeature(
            Predicate<Settings> isUsed,
            Function<SKU, SKUEntry> skus,
            DashboardSection dashboardSection,
            boolean blocking) {
        this(
                (sku, repGuild) -> List.of(),
                (sku, repGuild) -> isUsed.test(repGuild.settings()),
                skus,
                dashboardSection,
                blocking);
    }

    SupporterFeature(
            BiFunction<SKU, RepGuild, List<Replacement>> replacements,
            BiFunction<SKU, RepGuild, Boolean> isApplicable,
            Function<SKU, SKUEntry> skus,
            DashboardSection dashboardSection,
            boolean blocking) {
        this.replacements = replacements;
        this.isApplicable = isApplicable;
        this.skus = skus;
        this.dashboardSection = dashboardSection;
        this.blocking = blocking;
    }

    public SubscriptionError first() {
        return new SubscriptionError(this, Instant.now(), Instant.EPOCH, 0, false);
    }

    public List<Replacement> replacements(SKU sku, RepGuild repGuild) {
        return replacements.apply(sku, repGuild);
    }

    public String localeCode() {
        return "supportererror.%s.message".formatted(name().toLowerCase().replace("_", ""));
    }

    public SKUEntry skus(SKU sku) {
        return skus.apply(sku);
    }

    /**
     * The dashboard section in which the setting causing this feature can be changed.
     *
     * @return the section, or empty if the setting is not managed in the dashboard
     */
    public Optional<DashboardSection> dashboardSection() {
        return Optional.ofNullable(dashboardSection);
    }

    /**
     * Whether using this feature without entitlement stops reputation after the grace period.
     *
     * @return true if the feature blocks the guild once the warnings reach the threshold
     */
    public boolean isBlocking() {
        return blocking;
    }

    public boolean isApplicable(SKU sku, RepGuild repGuild) {
        return isApplicable.apply(sku, repGuild);
    }

    public boolean isEntitled(SKU sku, RepGuild repGuild) {
        return repGuild.subscriptions().isEntitled(skus(sku));
    }
}
