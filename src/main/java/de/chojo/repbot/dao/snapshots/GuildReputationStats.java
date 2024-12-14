/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots;

/**
 * Record representing the reputation statistics of a guild.
 *
 * @param totalReputation the total reputation of the guild
 * @param weekReputation the reputation gained by the guild in the past week
 * @param todayReputation the reputation gained by the guild today
 * @param topChannelId the ID of the top channel in the guild
 */
public record GuildReputationStats(int totalReputation, int weekReputation, int todayReputation, long topChannelId) {
}
