/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.premium;

import de.chojo.jdautil.interactions.base.SkuMeta;
import de.chojo.jdautil.interactions.premium.SKU;
import de.chojo.repbot.config.elements.sku.SKUEntry;
import de.chojo.repbot.dao.access.guild.RepGuild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.chojo.repbot.util.States.GRANT_ALL_SKU;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * POJO containing information about premium features for a guild.
 * Includes both simple boolean features and complex features with limits.
 * Each feature includes information about which SKUs can unlock it.
 */
public class PremiumFeaturesPOJO {
    private static final Logger log = getLogger(PremiumFeaturesPOJO.class);

    // Simple boolean features
    private final SimpleFeature reputationLog;
    private final SimpleFeature analyzerLog;
    private final SimpleFeature channelBlacklist;
    private final SimpleFeature localeOverrides;
    private final SimpleFeature autopost;
    private final SimpleFeature advancedRankings;
    private final SimpleFeature detailedProfile;
    private final SimpleFeature logChannel;
    private final SimpleFeature additionalEmojis;
    private final SimpleFeature profile;

    // Complex features with limits
    private final FeatureLimit reputationChannel;
    private final FeatureLimit reputationCategories;

    public PremiumFeaturesPOJO(
            SimpleFeature reputationLog,
            SimpleFeature analyzerLog,
            SimpleFeature channelBlacklist,
            SimpleFeature localeOverrides,
            SimpleFeature autopost,
            SimpleFeature advancedRankings,
            SimpleFeature detailedProfile,
            SimpleFeature logChannel,
            SimpleFeature additionalEmojis,
            SimpleFeature profile,
            FeatureLimit reputationChannel,
            FeatureLimit reputationCategories) {
        this.reputationLog = reputationLog;
        this.analyzerLog = analyzerLog;
        this.channelBlacklist = channelBlacklist;
        this.localeOverrides = localeOverrides;
        this.autopost = autopost;
        this.advancedRankings = advancedRankings;
        this.detailedProfile = detailedProfile;
        this.logChannel = logChannel;
        this.additionalEmojis = additionalEmojis;
        this.profile = profile;
        this.reputationChannel = reputationChannel;
        this.reputationCategories = reputationCategories;
    }

    public static PremiumFeaturesPOJO generate(RepGuild guild, ShardManager shardManager) {
        var subscriptions = guild.subscriptions();
        var features = guild.configuration().skus().features();
        var settings = guild.settings();

        // Resolve SKU information from Discord
        Map<Long, SkuInfo> skuMap = resolveSkus(shardManager);

        // Check simple boolean features
        SimpleFeature reputationLog = createSimpleFeature(
                subscriptions, features.reputationLog().extendedPages(), skuMap);
        SimpleFeature analyzerLog = createSimpleFeature(
                subscriptions, features.analyzerLog().longerLogTime(), skuMap);
        SimpleFeature channelBlacklist = createSimpleFeature(
                subscriptions, features.channelBlacklist().allow(), skuMap);
        SimpleFeature localeOverrides = createSimpleFeature(
                subscriptions, features.localeOverrides().reputationNameOverride(), skuMap);
        SimpleFeature autopost = createSimpleFeature(
                subscriptions, features.autopost().autopostChannel(), skuMap);
        SimpleFeature advancedRankings = createSimpleFeature(
                subscriptions, features.advancedRankings().advancedRankings(), skuMap);
        SimpleFeature detailedProfile = createSimpleFeature(
                subscriptions, features.detailedProfile().detailedProfile(), skuMap);
        SimpleFeature logChannel = createSimpleFeature(
                subscriptions, features.logChannel().logChannel(), skuMap);
        SimpleFeature additionalEmojis = createSimpleFeature(
                subscriptions, features.additionalEmojis().additionalEmojis(), skuMap);
        SimpleFeature profile = createSimpleFeature(
                subscriptions, features.profile().allow(), skuMap);

        // Check complex features with limits
        int defaultChannels = features.reputationChannel().defaultChannel();
        boolean moreChannelsUnlocked = isEntitled(subscriptions, features.reputationChannel().moreChannel());
        FeatureLimit reputationChannel = new FeatureLimit(
                moreChannelsUnlocked ? Integer.MAX_VALUE : defaultChannels,
                moreChannelsUnlocked,
                extractSkuInfos(features.reputationChannel().moreChannel(), skuMap)
        );

        int defaultCategories = features.reputationCategories().defaultCategories();
        boolean moreCategoriesUnlocked = isEntitled(subscriptions, features.reputationCategories().moreCategories());
        FeatureLimit reputationCategories = new FeatureLimit(
                moreCategoriesUnlocked ? Integer.MAX_VALUE : defaultCategories,
                moreCategoriesUnlocked,
                extractSkuInfos(features.reputationCategories().moreCategories(), skuMap)
        );

        return new PremiumFeaturesPOJO(
                reputationLog,
                analyzerLog,
                channelBlacklist,
                localeOverrides,
                autopost,
                advancedRankings,
                detailedProfile,
                logChannel,
                additionalEmojis,
                profile,
                reputationChannel,
                reputationCategories
        );
    }

    private static Map<Long, SkuInfo> resolveSkus(ShardManager shardManager) {
        try {
            List<net.dv8tion.jda.api.entities.SKU> skus = shardManager.getShards().getFirst()
                    .retrieveSKUList()
                    .complete();

            return skus.stream()
                    .collect(Collectors.toMap(
                            net.dv8tion.jda.api.entities.SKU::getIdLong,
                            sku -> new SkuInfo(sku.getId(), sku.getName())
                    ));
        } catch (Exception e) {
            log.error("Failed to resolve SKU list from Discord", e);
            return new HashMap<>();
        }
    }

    private static SimpleFeature createSimpleFeature(SkuMeta subscriptions, SKUEntry required, Map<Long, SkuInfo> skuMap) {
        boolean unlocked = isEntitled(subscriptions, required);
        List<SkuInfo> requiredSkus = extractSkuInfos(required, skuMap);
        return new SimpleFeature(unlocked, requiredSkus);
    }

    private static List<SkuInfo> extractSkuInfos(SKUEntry entry, Map<Long, SkuInfo> skuMap) {
        return entry.sku().stream()
                .map(SKU::skuId)
                .map(skuMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static boolean isEntitled(SkuMeta subscriptions, SkuMeta required) {
        if (GRANT_ALL_SKU) return true;
        return subscriptions.isEntitled(required);
    }

    public SimpleFeature reputationLog() {
        return reputationLog;
    }

    public SimpleFeature analyzerLog() {
        return analyzerLog;
    }

    public SimpleFeature channelBlacklist() {
        return channelBlacklist;
    }

    public SimpleFeature localeOverrides() {
        return localeOverrides;
    }

    public SimpleFeature autopost() {
        return autopost;
    }

    public SimpleFeature advancedRankings() {
        return advancedRankings;
    }

    public SimpleFeature detailedProfile() {
        return detailedProfile;
    }

    public SimpleFeature logChannel() {
        return logChannel;
    }

    public SimpleFeature additionalEmojis() {
        return additionalEmojis;
    }

    public SimpleFeature profile() {
        return profile;
    }

    public FeatureLimit reputationChannel() {
        return reputationChannel;
    }

    public FeatureLimit reputationCategories() {
        return reputationCategories;
    }
}
