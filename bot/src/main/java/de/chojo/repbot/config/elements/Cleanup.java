/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class Cleanup {
    private int analyzerLogHours = 24;
    private int gdprDays = 90;
    private int voiceActivityHours = 24;

    private int cleanupScheduleDays = 14;

    /**
     * The hours after an entry in the analyzer_log table gets deleted.
     */
    public int analyzerLogHours() {
        return analyzerLogHours;
    }

    /**
     * The days after an entry in the gdpr_log table gets deleted.
     */
    public int gdprDays() {
        return gdprDays;
    }

    /**
     * The hours after an entry in the voice_activity table gets deleted.
     */
    public int voiceActivityHours() {
        return voiceActivityHours;
    }

    /**
     * The days after an entry in the cleanup_schedule table gets deleted.
     */
    public int cleanupScheduleDays() {
        return cleanupScheduleDays;
    }
}
