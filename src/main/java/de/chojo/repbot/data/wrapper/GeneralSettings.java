package de.chojo.repbot.data.wrapper;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Optional;
import java.util.OptionalLong;

public class GeneralSettings {
    private final String prefix;
    private final boolean emojiDebug;
    private final long managerRole;

    public GeneralSettings() {
        prefix = null;
        emojiDebug = true;
        managerRole =0;
    }

    public GeneralSettings(String prefix, boolean emojiDebug, long managerRole) {
        this.prefix = prefix;
        this.emojiDebug = emojiDebug;
        this.managerRole = managerRole;
    }

    public Optional<String> prefix() {
        return Optional.ofNullable(prefix);
    }

    public boolean isManager(Member member) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;
        return member.getRoles().stream().anyMatch(role -> role.getIdLong() == managerRole);
    }

    public OptionalLong managerRole() {
        return managerRole == 0 ? OptionalLong.empty() : OptionalLong.of(managerRole);
    }

    public boolean isEmojiDebug() {
        return emojiDebug;
    }
}
