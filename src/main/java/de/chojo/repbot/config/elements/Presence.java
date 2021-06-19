package de.chojo.repbot.config.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Presence {
    private boolean active = false;
    private int interval = 5;
    private List<String> status = new ArrayList<>() {{
        add("Observing %guild_count% guilds!");
        add("Reading in %channel_count% channels!");
        add("%total_rep% Reputation received!");
        add("%weekly_rep% Reputation given this week!");
        add("%today_rep% Reputation given today!");
        add("%week_avg_rep% Reputation given per week!");
        add("%week_rep% Reputation given today!");
        add("Analyzed %analyzed_messages% during the last hour!");
    }};

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

    public int interval() {
        return interval;
    }
}
