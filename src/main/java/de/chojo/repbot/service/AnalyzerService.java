/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.dao.access.Analyzer;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for running the analyzer at scheduled intervals.
 */
public class AnalyzerService implements Runnable {
    private final Analyzer analyzer;

    /**
     * Constructs an AnalyzerService with the specified analyzer.
     *
     * @param analyzer the analyzer to be used by the service
     */
    private AnalyzerService(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * Creates and schedules an AnalyzerService.
     *
     * @param executorService the executor service to schedule the analyzer service
     * @param analyzer the analyzer to be used by the service
     */
    public static void create(ScheduledExecutorService executorService, Analyzer analyzer) {
        var analyzerService = new AnalyzerService(analyzer);
        executorService.scheduleAtFixedRate(analyzerService, 10, 60, TimeUnit.MINUTES);
    }

    /**
     * Runs the analyzer cleanup process.
     */
    @Override
    public void run() {
        analyzer.cleanup();
    }
}
