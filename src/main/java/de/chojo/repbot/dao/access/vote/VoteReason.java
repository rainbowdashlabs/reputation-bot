/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.vote;

public enum VoteReason {
    /**
     * A standard vote without any bonus.
     */
    STANDARD,
    /**
     * The user received an additional token because they are holding a streak.
     */
    STREAK,
    /**
     * The user received a bonus because they voted on every list.
     */
    BONUS,
    /**
     * The user transferred personal token to a guild.
     */
    TRANSFER,
    /**
     * The user purchased something with the token.
     */
    USE;
}
