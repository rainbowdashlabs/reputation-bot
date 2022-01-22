package de.chojo.repbot.util;

import net.dv8tion.jda.api.entities.Guild;

public final class Guilds {
    private Guilds() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static String prettyName(Guild guild) {
        return String.format("%s (%s)", guild.getName(), guild.getIdLong());
    }
}
