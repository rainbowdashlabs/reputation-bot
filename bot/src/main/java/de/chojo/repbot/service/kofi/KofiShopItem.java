/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.kofi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KofiShopItem(
        @JsonProperty("direct_link_code") String directLinkCode,
        @JsonProperty("item_name") String itemName,
        @JsonProperty("quantity") int quantity) {}
