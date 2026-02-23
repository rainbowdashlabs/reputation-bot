/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.tokenpurchaseservice;

public enum FailureReason {
    UNKNOWN_FEATURE,
    INSUFFICIENT_USER_TOKENS,
    INSUFFICIENT_GUILD_TOKENS,
    GUILD_HAS_SUBSCRIPTION,
    GUILD_NOT_FOUND,
    UNKNOWN;

    private final String localeKey;

    FailureReason() {
        localeKey = "failurereason." + name().toLowerCase();
    }

    public String localeKey() {
        return localeKey;
    }
}
