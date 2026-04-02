/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.premium;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.jdautil.interactions.base.SkuMeta;
import de.chojo.jdautil.interactions.premium.SKU;
import de.chojo.repbot.config.elements.sku.SKUEntry;
import de.chojo.repbot.config.elements.sku.Subscription;
import de.chojo.repbot.dao.access.guild.RepGuild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.chojo.repbot.util.States.GRANT_ALL_SKU;
import static de.chojo.repbot.util.States.TEST_MODE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * POJO containing information about premium features for a guild.
 * Includes both simple boolean features and complex features with limits.
 * Each feature includes information about which SKUs can unlock it.
 */
public class PremiumFeaturesPOJO {
    private static final Logger log = getLogger(PremiumFeaturesPOJO.class);

    private static final Cache<Long, SkuInfo> SKU_CACHE =
            CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build();

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
    private final SimpleFeature integrationBypass;

    // Complex features with limits
    private final FeatureLimit reputationChannel;
    private final FeatureLimit reputationCategories;

    private final List<String> activeSkus;

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
            SimpleFeature integrationBypass,
            FeatureLimit reputationChannel,
            FeatureLimit reputationCategories,
            List<String> activeSkus) {
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
        this.integrationBypass = integrationBypass;
        this.reputationChannel = reputationChannel;
        this.reputationCategories = reputationCategories;
        this.activeSkus = activeSkus;
    }

    public static PremiumFeaturesPOJO generate(RepGuild guild, ShardManager shardManager) {
        var subscriptions = guild.subscriptions();
        var features = guild.configuration().skus().features();

        // Resolve SKU information from Discord
        Map<Long, SkuInfo> skuMap = resolveSkus(shardManager);

        // Check simple boolean features
        SimpleFeature reputationLog =
                createSimpleFeature(subscriptions, features.reputationLog().fullSkuEntry(), skuMap);
        SimpleFeature analyzerLog =
                createSimpleFeature(subscriptions, features.analyzerLog().fullSkuEntry(), skuMap);
        SimpleFeature channelBlacklist =
                createSimpleFeature(subscriptions, features.channelBlacklist().fullSkuEntry(), skuMap);
        SimpleFeature localeOverrides =
                createSimpleFeature(subscriptions, features.localeOverrides().fullSkuEntry(), skuMap);
        SimpleFeature autopost =
                createSimpleFeature(subscriptions, features.autopost().fullSkuEntry(), skuMap);
        SimpleFeature advancedRankings =
                createSimpleFeature(subscriptions, features.advancedRankings().fullSkuEntry(), skuMap);
        SimpleFeature detailedProfile =
                createSimpleFeature(subscriptions, features.detailedProfile().fullSkuEntry(), skuMap);
        SimpleFeature logChannel =
                createSimpleFeature(subscriptions, features.logChannel().fullSkuEntry(), skuMap);
        SimpleFeature additionalEmojis =
                createSimpleFeature(subscriptions, features.additionalEmojis().fullSkuEntry(), skuMap);
        SimpleFeature profile =
                createSimpleFeature(subscriptions, features.profile().fullSkuEntry(), skuMap);

        SimpleFeature integrationBypass =
                createSimpleFeature(subscriptions, features.integrationBypass().fullSkuEntry(), skuMap);

        // Check complex features with limits
        int defaultChannels = features.reputationChannel().defaultChannel();
        boolean moreChannelsUnlocked =
                isEntitled(subscriptions, features.reputationChannel().fullSkuEntry());
        FeatureLimit reputationChannel = new FeatureLimit(
                moreChannelsUnlocked ? Integer.MAX_VALUE : defaultChannels,
                moreChannelsUnlocked,
                extractSkuInfos(features.reputationChannel().fullSkuEntry(), skuMap));

        int defaultCategories = features.reputationCategories().defaultCategories();
        boolean moreCategoriesUnlocked =
                isEntitled(subscriptions, features.reputationCategories().fullSkuEntry());
        FeatureLimit reputationCategories = new FeatureLimit(
                moreCategoriesUnlocked ? Integer.MAX_VALUE : defaultCategories,
                moreCategoriesUnlocked,
                extractSkuInfos(features.reputationCategories().fullSkuEntry(), skuMap));

        List<String> active;
        if (TEST_MODE && GRANT_ALL_SKU) {
            active = guild.configuration().skus().subscriptions().stream().map(Subscription::subscriptionSku).map(String::valueOf).toList();
        } else {
            active = subscriptions.sku().stream().map(s -> String.valueOf(s.skuId())).toList();
        }

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
                integrationBypass,
                reputationChannel,
                reputationCategories,
                active);
    }

    private static Map<Long, SkuInfo> resolveSkus(ShardManager shardManager) {
        var cachedSkus = SKU_CACHE.asMap();
        if (!cachedSkus.isEmpty()) {
            return Collections.unmodifiableMap(cachedSkus);
        }

        try {
            List<net.dv8tion.jda.api.entities.SKU> skus =
                    shardManager.getShards().getFirst().retrieveSKUList().complete();

            Map<Long, SkuInfo> resolvedSkus = skus.stream()
                                                  .collect(Collectors.toMap(
                                                          net.dv8tion.jda.api.entities.SKU::getIdLong,
                                                          sku -> new SkuInfo(sku.getId(), sku.getName())));

            SKU_CACHE.putAll(resolvedSkus);
            return resolvedSkus;
        } catch (Exception e) {
            log.error("Failed to resolve SKU list from Discord", e);
            return new HashMap<>();
        }
    }

    private static SimpleFeature createSimpleFeature(
            SkuMeta subscriptions, SKUEntry required, Map<Long, SkuInfo> skuMap) {
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

    public List<String> activeSkus() {
        return activeSkus;
    }

    public SimpleFeature integrationBypass() {
        return integrationBypass;
    }
}
