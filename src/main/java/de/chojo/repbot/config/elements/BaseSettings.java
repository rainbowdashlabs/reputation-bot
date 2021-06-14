package de.chojo.repbot.config.elements;

import java.util.ArrayList;
import java.util.List;

public class BaseSettings {
    private String token = "";
    private String defaultPrefix = "!";
    private boolean exclusiveHelp = false;
    private boolean internalCommands = false;
    private List<Long> botOwner = new ArrayList<>();

    public String token() {
        return token;
    }

    public boolean isOwner(long id) {
        return botOwner.contains(id);
    }

    public String defaultPrefix() {
        return defaultPrefix;
    }

    public boolean isInternalCommands() {
        return internalCommands;
    }

    public boolean isExclusiveHelp() {
        return exclusiveHelp;
    }
}
