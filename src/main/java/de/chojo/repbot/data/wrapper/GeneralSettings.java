package de.chojo.repbot.data.wrapper;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Optional;
import java.util.OptionalLong;

public class GeneralSettings {
    private final String prefix;
    private final boolean emojiDebug;
    private final long managerRole;
    private final boolean stackRoles;

    public GeneralSettings() {
        prefix = null;
        emojiDebug = true;
        managerRole = 0;
        stackRoles = false;
    }

    public GeneralSettings(String prefix, boolean emojiDebug, long managerRole, boolean stackRoles) {
        this.prefix = prefix;
        this.emojiDebug = emojiDebug;
        this.managerRole = managerRole;
        this.stackRoles = stackRoles;
    }

    public Optional<String> prefix() {
        return Optional.ofNullable(prefix);
    }

    public boolean isManager(Member member) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;
        return member.getRoles().stream().anyMatch(role -> role.getIdLong() == managerRole);
    }

    public Optional<Long> managerRole() {
        return managerRole == 0 ? Optional.empty() : Optional.of(managerRole);
    }

    public boolean isEmojiDebug() {
        return emojiDebug;
    }

    public boolean isStackRoles() {
        return stackRoles;
    }
}
