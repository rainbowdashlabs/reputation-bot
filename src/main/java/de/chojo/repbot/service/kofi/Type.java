/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.kofi;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Type {
    @JsonProperty("Subscription")
    SUBSCRIPTION,
    @JsonProperty("Donation")
    DONATION,
    @JsonProperty("Commission")
    COMMISSION,
    @JsonProperty("Shop Order")
    SHOP_ORDER
}
