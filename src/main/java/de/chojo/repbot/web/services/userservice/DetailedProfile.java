/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.services.userservice;

import de.chojo.repbot.dao.snapshots.ChannelStats;
import de.chojo.repbot.web.pojo.ranking.RankingEntryStatPOJO;

import java.util.List;

/**
 * Additional profile data available when the guild has the detailedProfile feature unlocked.
 *
 * @param topDonors            top users this user has given reputation to
 * @param topReceivers         top users this user has received reputation from
 * @param mostGivenChannels    channels where this user has given the most reputation
 * @param mostReceivedChannels channels where this user has received the most reputation
 */
public record DetailedProfile(
        List<RankingEntryStatPOJO> topDonors,
        List<RankingEntryStatPOJO> topReceivers,
        List<ChannelStats> mostGivenChannels,
        List<ChannelStats> mostReceivedChannels) {}
