/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Represents the self-cleanup configuration for the bot.
 * This class provides settings for managing inactive users and prompting them for cleanup.
 */
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class SelfCleanup {
    /**
     * Indicates if the self-cleanup is active.
     */
    private boolean active = true;

    /**
     * Number of days before prompting for cleanup.
     */
    private int promptDays = 3;

    /**
     * Number of days before leaving after prompt.
     */
    private int leaveDays = 3;

    /**
     * Number of days of inactivity before cleanup.
     */
    private int inactiveDays = 90;

    /**
     * Creates a new self-cleanup configuration with default values.
     */
    public SelfCleanup(){
    }

    /**
     * Checks if the self-cleanup is active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Retrieves the number of days before prompting for cleanup.
     *
     * @return the number of prompt days
     */
    public int promptDays() {
        return promptDays;
    }

    /**
     * Retrieves the number of days before leaving after prompt.
     *
     * @return the number of leave days
     */
    public int leaveDays() {
        return leaveDays;
    }

    /**
     * Retrieves the number of days of inactivity before cleanup.
     *
     * @return the number of inactive days
     */
    public int inactiveDays() {
        return inactiveDays;
    }

    /**
     * Calculates the offset date-time for the prompt days.
     *
     * @return the offset date-time for the prompt days
     */
    public OffsetDateTime getPromptDaysOffset() {
        return LocalDateTime.now().minusDays(promptDays()).atOffset(ZoneOffset.UTC);
    }

    /**
     * Calculates the local date-time for the leave days.
     *
     * @return the local date-time for the leave days
     */
    public LocalDateTime getLeaveDaysOffset() {
        return LocalDateTime.now().minusDays(leaveDays());
    }

    /**
     * Calculates the local date-time for the inactive days.
     *
     * @return the local date-time for the inactive days
     */
    public LocalDateTime getInactiveDaysOffset() {
        return LocalDateTime.now().minusDays(inactiveDays());
    }
}
