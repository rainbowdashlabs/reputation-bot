package de.chojo.repbot.util;

import net.dv8tion.jda.api.entities.Role;

public final class Roles {
    private Roles() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static String prettyName(Role role) {
        return String.format("%s (%s)", role.getName(), role.getIdLong());
    }
}
