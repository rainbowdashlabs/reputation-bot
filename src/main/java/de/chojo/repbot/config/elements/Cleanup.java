package de.chojo.repbot.config.elements;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class Cleanup {
    private int analyzerLogHours = 24;
    private int gdprDays = 90;
    private int voiceActivityHours = 24;

    private int cleanupScheduleDays = 14;

    public int analyzerLogHours() {
        return analyzerLogHours;
    }

    public int gdprDays() {
        return gdprDays;
    }

    public int voiceActivityHours() {
        return voiceActivityHours;
    }

    public int cleanupScheduleDays() {
        return cleanupScheduleDays;
    }
}
