/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.statistics;

/**
 * Interface for providing chart data.
 */
public interface ChartProvider {

    /**
     * Generates a chart with the specified title.
     *
     * @param title the title of the chart
     * @return a byte array representing the chart
     */
    byte[] getChart(String title);
}
