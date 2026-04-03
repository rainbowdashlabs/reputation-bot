/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.services.userservice;

import de.chojo.repbot.web.pojo.guild.MemberPOJO;
import org.jetbrains.annotations.Nullable;

/**
 * Profile data for a user within a guild.
 *
 * @param member          the member's display data
 * @param rank            the user's received reputation rank
 * @param rankDonated     the user's donated reputation rank
 * @param reputation      the user's total reputation
 * @param donated         the user's total donated reputation
 * @param level           the role ID of the user's current reputation rank role, or null if none
 * @param currentProgress the user's reputation progress towards the next level
 * @param nextLevelReputation the reputation required to reach the next level, or null if at max
 * @param detailedProfile additional detailed profile data, present if the guild has the feature unlocked
 * @param adminProfile    additional admin profile data, present if the requesting user is a guild admin
 */
public record UserProfile(
        MemberPOJO member,
        long rank,
        long rankDonated,
        long reputation,
        long donated,
        @Nullable Long level,
        long currentProgress,
        @Nullable Long nextLevelReputation,
        @Nullable DetailedProfile detailedProfile,
        @Nullable AdminProfile adminProfile) {}
