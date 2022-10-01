package de.chojo.repbot.service;

import de.chojo.repbot.dao.access.Analyzer;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AnalyzerService implements Runnable {
    private final Analyzer analyzer;

    private AnalyzerService(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public static void create(ScheduledExecutorService executorService, Analyzer analyzer) {
        var analyzerService = new AnalyzerService(analyzer);
        executorService.scheduleAtFixedRate(analyzerService, 10, 60, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        analyzer.cleanup();
    }
}
