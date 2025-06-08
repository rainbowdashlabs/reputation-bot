/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.config.elements.SKU;
import de.chojo.repbot.config.elements.sku.SKUEntry;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.subscriptions.SubscriptionError;

import java.time.Instant;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static de.chojo.jdautil.localization.util.Replacement.create;

public enum SupporterFeature {
    CHANNEL_LIMIT_EXCEEDED(
            (sku, repGuild) -> List.of(create("ALLOWED", sku.features().reputationChannel().defaultChannel()), create("COUNT", repGuild.settings().thanking().channels().categories().size())),
            (sku, repGuild) -> repGuild.subscriptions().isEntitled(sku.features().reputationChannel().moreChannel()),
            (sku, repGuild) -> repGuild.settings().thanking().channels().categories().size() > sku.features().reputationChannel().defaultChannel(),
            sku -> sku.features().reputationChannel().moreChannel()),
    CATEGORY_LIMIT_EXCEEDED(
            (sku, repGuild) -> List.of(create("ALLOWED", sku.features().reputationCategories().defaultCategories()), create("COUNT", repGuild.settings().thanking().channels().categories().size())),
            (sku, repGuild) -> repGuild.subscriptions().isEntitled(sku.features().reputationCategories().moreCategories()),
            (sku, repGuild) -> repGuild.settings().thanking().channels().categories().size() > sku.features().reputationCategories().defaultCategories(),
            sku -> sku.features().reputationCategories().moreCategories()),
    BLACKLIST_USED(
            (sku, repGuild) -> List.of(),
            (sku, repGuild) -> repGuild.subscriptions().isEntitled(sku.features().channelBlacklist().allow()),
            (sku, repGuild) -> !repGuild.settings().thanking().channels().isWhitelist(),
            sku -> sku.features().channelBlacklist().allow()),
    BOT_NICKNAMED((sku, repGuild) -> List.of(),
            (sku, repGuild) -> repGuild.subscriptions().isEntitled(sku.features().nickname().allow()),
            (sku, repGuild) -> repGuild.guild().getSelfMember().getNickname() != null,
            sku -> sku.features().nickname().allow());

    private final BiFunction<SKU, RepGuild, List<Replacement>> replacements;
    private final BiFunction<SKU, RepGuild, Boolean> isEntitled;
    private final BiFunction<SKU, RepGuild, Boolean> isApplicable;
    private final Function<SKU, SKUEntry> skus;

    SupporterFeature(BiFunction<SKU, RepGuild, List<Replacement>> replacements,
                     BiFunction<SKU, RepGuild, Boolean> isEntitled,
                     BiFunction<SKU, RepGuild, Boolean> isApplicable,
                     Function<SKU, SKUEntry> skus) {
        this.replacements = replacements;
        this.isEntitled = isEntitled;
        this.isApplicable = isApplicable;
        this.skus = skus;
    }

    public SubscriptionError first() {
        return new SubscriptionError(this, Instant.now(), Instant.EPOCH, 0, row.getBoolean("notified"));
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

    public boolean isApplicable(SKU sku, RepGuild repGuild) {
        return isApplicable.apply(sku, repGuild);
    }

    public Boolean isEntitled(SKU sku, RepGuild repGuild) {
        return isEntitled.apply(sku, repGuild);
    }
}
