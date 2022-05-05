package de.chojo.repbot.data.wrapper;

import java.util.Optional;

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
