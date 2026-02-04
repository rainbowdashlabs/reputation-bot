/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.validation;

import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.web.error.PremiumFeatureException;
import de.chojo.repbot.web.pojo.premium.FeatureLimit;
import de.chojo.repbot.web.pojo.premium.PremiumFeaturesPOJO;
import de.chojo.repbot.web.pojo.premium.SimpleFeature;
import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * Validator for premium features.
 * Ensures users can't enable or exceed limits of premium features without proper entitlement.
 */
public class PremiumValidator {
    private final PremiumFeaturesPOJO features;

    public PremiumValidator(RepGuild guild, ShardManager shardManager) {
        this.features = PremiumFeaturesPOJO.generate(guild, shardManager);
    }

    /**
     * Validates that a simple boolean feature is unlocked.
     *
     * @param feature     The feature to check
     * @param featureName Display name for error messages
     * @throws PremiumFeatureException if the feature is not unlocked
     */
    public void requireFeature(SimpleFeature feature, String featureName) {
        if (!feature.unlocked()) {
            throw new PremiumFeatureException(featureName, feature.requiredSkus());
        }
    }

    /**
     * Validates that a feature requiring true value is unlocked.
     *
     * @param value       The value being set
     * @param feature     The feature to check
     * @param featureName Display name for error messages
     * @throws PremiumFeatureException if trying to enable a locked feature
     */
    public void requireFeatureIfEnabled(boolean value, SimpleFeature feature, String featureName) {
        if (value && !feature.unlocked()) {
            throw new PremiumFeatureException(featureName, feature.requiredSkus());
        }
    }

    /**
     * Validates that a count doesn't exceed the allowed limit.
     *
     * @param count       The count to validate
     * @param limit       The feature limit
     * @param featureName Display name for error messages
     * @throws PremiumFeatureException if the count exceeds the limit
     */
    public void requireWithinLimit(int count, FeatureLimit limit, String featureName) {
        if (count > limit.max() && !limit.unlocked()) {
            throw new PremiumFeatureException(
                    featureName,
                    limit.requiredSkus(),
                    count,
                    limit.max()
            );
        }
    }

    /**
     * Validates that whitelist mode is allowed (blacklist requires premium).
     *
     * @param isWhitelist Whether whitelist mode is being used
     * @throws PremiumFeatureException if trying to use blacklist without premium
     */
    public void requireWhitelistOrPremium(boolean isWhitelist) {
        if (!isWhitelist && !features.channelBlacklist().unlocked()) {
            throw new PremiumFeatureException("Channel Blacklist", features.channelBlacklist().requiredSkus());
        }
    }

    public PremiumFeaturesPOJO features() {
        return features;
    }
}
