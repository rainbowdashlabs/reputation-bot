/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

/**
 * Configuration settings for the analyzer.
 */
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class AnalyzerSettings {
    private int historySize = 100;
    private int voiceMembers = 10;
    private int latestMaxHours = 12;
    private float minFuzzyScore = 0.9f;

    /**
     * Creates a new analyzer configuration with default values.
     */
    public AnalyzerSettings(){
    }

    /**
     * Gets the maximum number of hours for the latest entries.
     *
     * @return the maximum number of hours for the latest entries
     */
    public int latestMaxHours() {
        return latestMaxHours;
    }

    /**
     * Gets the minimum fuzzy score.
     *
     * @return the minimum fuzzy score
     */
    public float minFuzzyScore() {
        return minFuzzyScore;
    }

    /**
     * Gets the history size, limited to a maximum of 100.
     *
     * @return the history size
     */
    public int historySize() {
        return Math.min(historySize, 100);
    }

    /**
     * Gets the number of voice members.
     *
     * @return the number of voice members
     */
    public int voiceMembers() {
        return voiceMembers;
    }
}
