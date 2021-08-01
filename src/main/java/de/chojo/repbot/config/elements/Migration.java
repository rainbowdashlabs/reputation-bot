package de.chojo.repbot.config.elements;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Migration {
    private boolean active;
    private String migrationMessage = "Please migrate";
    private int maxMigrations = 3;
    private int maxMigrationsPeriod = 7;

    public boolean isActive() {
        return active;
    }

    public String migrationMessage() {
        return migrationMessage;
    }

    public int maxMigrations() {
        return maxMigrations;
    }

    public int maxMigrationsPeriod() {
        return maxMigrationsPeriod;
    }
}
