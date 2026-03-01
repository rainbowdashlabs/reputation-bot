/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild.features;

import de.chojo.repbot.service.tokenpurchaseservice.FailureReason;
import de.chojo.repbot.service.tokenpurchaseservice.PurchaseResult;
import org.jetbrains.annotations.Nullable;

public record FeaturePurchaseResultPOJO(
        boolean success, @Nullable FailureReason reason) {
    public static FeaturePurchaseResultPOJO generate(PurchaseResult result) {
        return new FeaturePurchaseResultPOJO(result.success(), result.reason());
    }
}
