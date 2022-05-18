package de.chojo.repbot.config.elements;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class TestMode {
    private boolean testMode;
    private long[] testGuilds = {0L};

    public boolean isTestMode() {
        return testMode;
    }

    public long[] testGuilds() {
        return testGuilds;
    }
}
