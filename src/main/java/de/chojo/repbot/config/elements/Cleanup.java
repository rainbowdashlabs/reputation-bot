/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

/**
 * Configuration class for the cleanup settings.
 */
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class Cleanup {
    /**
     * The hours after an entry in the analyzer\_log table gets deleted.
     */
    private int analyzerLogHours = 24;

    /**
     * The days after an entry in the gdpr\_log table gets deleted.
     */
    private int gdprDays = 90;

    /**
     * The hours after an entry in the voice\_activity table gets deleted.
     */
    private int voiceActivityHours = 24;

    /**
     * The days after an entry in the cleanup\_schedule table gets deleted.
     */
    private int cleanupScheduleDays = 14;

    /**
     * Creates a new cleanup configuration with default values.
     */
    public Cleanup(){
    }

    /**
     * Returns the hours after an entry in the analyzer\_log table gets deleted.
     *
     * @return the analyzer log hours
     */
    public int analyzerLogHours() {
        return analyzerLogHours;
    }

    /**
     * Returns the days after an entry in the gdpr\_log table gets deleted.
     *
     * @return the GDPR days
     */
    public int gdprDays() {
        return gdprDays;
    }

    /**
     * Returns the hours after an entry in the voice\_activity table gets deleted.
     *
     * @return the voice activity hours
     */
    public int voiceActivityHours() {
        return voiceActivityHours;
    }

    /**
     * Returns the days after an entry in the cleanup\_schedule table gets deleted.
     *
     * @return the cleanup schedule days
     */
    public int cleanupScheduleDays() {
        return cleanupScheduleDays;
    }
}
