package de.chojo.repbot.dao.access.guild.settings.sub;

public enum CooldownDirection {
    UNIDIRECTIONAL,
    BIDIRECTIONAL;

    public String localCode() {
        return getClass().getSimpleName().toLowerCase() + "." + this.name().toLowerCase();
    }
}
