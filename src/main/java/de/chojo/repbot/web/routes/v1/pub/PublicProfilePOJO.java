/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.pub;

import de.chojo.repbot.web.pojo.guild.MemberPOJO;

/**
 * A minimal public profile exposing only reputation and rank for a user.
 *
 * @param member     the member's display data
 * @param rank       the user's received reputation rank
 * @param reputation the user's total reputation
 */
public record PublicProfilePOJO(MemberPOJO member, long rank, long reputation) {}
