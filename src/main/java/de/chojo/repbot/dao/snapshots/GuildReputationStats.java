/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots;

public record GuildReputationStats(int totalReputation, int weekReputation, int todayReputation, long topChannelId) {
}
