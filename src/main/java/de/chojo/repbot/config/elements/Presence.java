package de.chojo.repbot.config.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Presence {
    private boolean active = false;
    private List<String> status = new ArrayList<>();

    public boolean isActive() {
        return active;
    }

    public List<String> status() {
        return status;
    }

    public String randomStatus() {
        if (status.isEmpty()) return "";
        return status.get(ThreadLocalRandom.current().nextInt(status.size()));
    }
}
