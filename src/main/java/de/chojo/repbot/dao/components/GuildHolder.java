package de.chojo.repbot.dao.components;

import net.dv8tion.jda.api.entities.Guild;

public interface GuildHolder {
    Guild guild();

    default long guildId() {
        return guild().getIdLong();
    }
}
