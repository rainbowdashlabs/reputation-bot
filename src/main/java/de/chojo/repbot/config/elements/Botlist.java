package de.chojo.repbot.config.elements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class Botlist {
    private boolean submit = false;
    private List<Long> guildIds = new ArrayList<>();
    private String topGg = "";
    private String discordBotsGg = "";
    private String discordBotlistCom = "";

    public boolean isSubmit() {
        return submit;
    }

    public String topGg() {
        return topGg;
    }

    public String discordBotsGg() {
        return discordBotsGg;
    }

    public String discordBotlistCom() {
        return discordBotlistCom;
    }

    public boolean isBotlistGuild(long id) {
        return guildIds.contains(id);
    }
}
