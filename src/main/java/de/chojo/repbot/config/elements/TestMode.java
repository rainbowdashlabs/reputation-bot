package de.chojo.repbot.config.elements;

public class TestMode {
    private boolean testMode = false;
    private long[] testGuilds = {0L};

    public boolean isTestMode() {
        return testMode;
    }

    public long[] testGuilds() {
        return testGuilds;
    }
}
