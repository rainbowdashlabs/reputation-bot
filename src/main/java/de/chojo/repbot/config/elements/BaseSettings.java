package de.chojo.repbot.config.elements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal", "MismatchedQueryAndUpdateOfCollection"})
public class BaseSettings {
    private String token = "";
    private boolean internalCommands;
    private List<Long> botOwner = new ArrayList<>();
    private long botGuild = 0L;

    public String token() {
        return token;
    }

    public boolean isOwner(long id) {
        return botOwner.contains(id);
    }

    public boolean isInternalCommands() {
        return internalCommands;
    }

    public long botGuild() {
        return botGuild;
    }
}
