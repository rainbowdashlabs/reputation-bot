package de.chojo.repbot.data.wrapper;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Optional;
import java.util.OptionalLong;

public class GeneralSettings {
    private final String prefix;
    private final boolean emojiDebug;
    private final boolean stackRoles;

    public GeneralSettings() {
        prefix = null;
        emojiDebug = true;
        stackRoles = false;
    }

    public GeneralSettings(String prefix, boolean emojiDebug, boolean stackRoles) {
        this.prefix = prefix;
        this.emojiDebug = emojiDebug;
        this.stackRoles = stackRoles;
    }

    public Optional<String> prefix() {
        return Optional.ofNullable(prefix);
    }

    public boolean isEmojiDebug() {
        return emojiDebug;
    }

    public boolean isStackRoles() {
        return stackRoles;
    }
}
