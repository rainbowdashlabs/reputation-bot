/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

public class Voting {
    private int minDaysStreak = 7;
    private int hoursSteak = 28;

    public int minDaysStreak() {
        return minDaysStreak;
    }

    public int hoursSteak() {
        return hoursSteak;
    }
}
