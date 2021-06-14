package de.chojo.repbot.config.elements;

@SuppressWarnings("FieldMayBeFinal")
public class Botlist {
    private boolean submit = false;
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
}
