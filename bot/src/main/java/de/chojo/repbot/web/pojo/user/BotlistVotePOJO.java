/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.user;

import java.time.Instant;

public record BotlistVotePOJO(String name, String voteUrl, Instant lastVote, int streak) {}
