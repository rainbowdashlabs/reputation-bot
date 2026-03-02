/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.kofi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties({"shipping"})
public record KofiTransaction(
        @JsonProperty("verification_token") String verificationToken,
        @JsonProperty("message_id") String messageId,
        Instant timestamp,
        Type type,
        @JsonProperty("is_public") boolean isPublic,
        @JsonProperty("from_name") String fromName,
        String message,
        String amount,
        String url,
        String email,
        String currency,
        @JsonProperty("is_subscription_payment") boolean isSubscriptionPayment,
        @JsonProperty("is_first_subscription_payment") boolean isFirstSubscriptionPayment,
        @JsonProperty("kofi_transaction_id") String kofiTransactionId,
        @JsonProperty("shop_items") List<KofiShopItem> shopItems,
        @JsonProperty("shipping") Object shipping,
        @JsonProperty("tier_name") String tierName,
        @JsonProperty("discord_username") String discordUsername,
        @JsonProperty("discord_userid") String discordUserId) {}
