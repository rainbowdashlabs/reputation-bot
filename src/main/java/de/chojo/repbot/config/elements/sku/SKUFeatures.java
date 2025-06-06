/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements.sku;

import de.chojo.repbot.config.elements.sku.feature.AdditionalEmojis;
import de.chojo.repbot.config.elements.sku.feature.AdvancedRankings;
import de.chojo.repbot.config.elements.sku.feature.AnalyzerLog;
import de.chojo.repbot.config.elements.sku.feature.Autopost;
import de.chojo.repbot.config.elements.sku.feature.ChannelBlacklist;
import de.chojo.repbot.config.elements.sku.feature.DetailedProfile;
import de.chojo.repbot.config.elements.sku.feature.LocaleOverrides;
import de.chojo.repbot.config.elements.sku.feature.LogChannel;
import de.chojo.repbot.config.elements.sku.feature.Nickname;
import de.chojo.repbot.config.elements.sku.feature.ReputationCategories;
import de.chojo.repbot.config.elements.sku.feature.ReputationChannel;
import de.chojo.repbot.config.elements.sku.feature.ReputationLog;

public class SKUFeatures {
    ReputationLog reputationLog = new ReputationLog();
    AnalyzerLog analyzerLog = new AnalyzerLog();
    ReputationCategories reputationCategories = new ReputationCategories();
    ReputationChannel reputationChannel = new ReputationChannel();
    ChannelBlacklist channelBlacklist = new ChannelBlacklist();
    LocaleOverrides localeOverrides = new LocaleOverrides();
    Autopost autopost = new Autopost();
    AdvancedRankings advancedRankings = new AdvancedRankings();
    DetailedProfile detailedProfile = new DetailedProfile();
    LogChannel logChannel = new LogChannel();
    AdditionalEmojis additionalEmojis = new AdditionalEmojis();
    Nickname nickname = new Nickname();

    public ReputationLog reputationLog() {
        return reputationLog;
    }

    public AnalyzerLog analyzerLog() {
        return analyzerLog;
    }

    public ReputationCategories reputationCategories() {
        return reputationCategories;
    }
    public ReputationChannel reputationChannel() {
        return reputationChannel;
    }

    public LocaleOverrides localeOverrides() {
        return localeOverrides;
    }

    public Autopost autopost() {
        return autopost;
    }

    public AdvancedRankings advancedRankings() {
        return advancedRankings;
    }

    public DetailedProfile detailedProfile() {
        return detailedProfile;
    }

    public LogChannel logChannel() {
        return logChannel;
    }

    public AdditionalEmojis additionalEmojis() {
        return additionalEmojis;
    }

    public ChannelBlacklist channelBlacklist() {
        return channelBlacklist;
    }

    public Nickname nickname() {
        return nickname;
    }
}
