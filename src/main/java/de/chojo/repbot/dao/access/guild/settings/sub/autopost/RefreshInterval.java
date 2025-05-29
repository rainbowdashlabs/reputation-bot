/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.autopost;

public enum RefreshInterval {
    /**
     * Send on every full hour.
     */
    HOURLY,
    /**
     * Send at midnight UTC.
     */
    DAILY,
    /**
     * Send at midnight UTC every Monday.
     */
    WEEKLY,
    /**
     * Send at midnight UTC every first day of the month.
     */
    MONTHLY;

    @Override
    public String toString() {
        return "%s.%s.name".formatted(getClass().getName().toLowerCase(), name().toLowerCase());
    }
}
