package de.chojo.repbot.config.elements;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class SelfCleanup {
    private boolean active = true;
    private int promptDays = 3;
    private int leaveDays = 3;
    private int inactiveDays = 30;

    public boolean isActive() {
        return active;
    }

    public int promptDays() {
        return promptDays;
    }

    public int leaveDays() {
        return leaveDays;
    }

    public int inactiveDays() {
        return inactiveDays;
    }

    public OffsetDateTime getPromptDaysOffset() {
        return LocalDateTime.now().minusDays(promptDays()).atOffset(ZoneOffset.UTC);
    }

    public LocalDateTime getLeaveDaysOffset() {
        return LocalDateTime.now().minusDays(leaveDays());
    }

    public LocalDateTime getInactiveDaysOffset() {
        return LocalDateTime.now().minusDays(inactiveDays());
    }
}
