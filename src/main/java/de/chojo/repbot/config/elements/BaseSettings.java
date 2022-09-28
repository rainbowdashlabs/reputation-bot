package de.chojo.repbot.config.elements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal", "MismatchedQueryAndUpdateOfCollection"})
public class BaseSettings {
    private String token = "";
    private List<Long> botOwner = new ArrayList<>();
    private long botGuild = 0L;

    public String token() {
        return token;
    }

    public boolean isOwner(long id) {
        return botOwner.contains(id);
    }

    public long botGuild() {
        return botGuild;
    }
}
