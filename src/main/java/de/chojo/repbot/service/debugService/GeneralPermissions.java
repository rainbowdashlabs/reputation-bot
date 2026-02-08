/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.debugService;

import net.dv8tion.jda.api.Permission;

import java.util.function.Function;

public enum GeneralPermissions {
    MANAGE_ROLES(Permission.MANAGE_ROLES, "Manage Roles", "Reputation roles can't be given."),
    NICKNAME_CHANGE(Permission.NICKNAME_CHANGE, "Change Nickname", "Nickname can't be changed in the dashboard."),
    MANAGE_GUILD_EXPRESSIONS(
            Permission.MANAGE_GUILD_EXPRESSIONS,
            "Manage Guild Expressions",
            "Custom reactions can't be suggested in the dashboard."),
    VIEW_CHANNEL(
            Permission.VIEW_CHANNEL,
            "View Channel",
            "Reputation channels can't be seen. Reputation can't be given via any text based method."),
    MESSAGE_HISTORY(
            Permission.MESSAGE_HISTORY,
            "Message History",
            "Reputation confirmation via embeds is not working. Most abuse protection checks will fail."),
    MESSAGE_ADD_REACTION(
            Permission.MESSAGE_ADD_REACTION, "Add Reactions", "Reputation confirmation via reactions is not working."),
    MESSAGE_SEND(
            Permission.MESSAGE_SEND,
            "Send Messages",
            "Any confirmation message won't work. For example reaction confirmation or reputation confirmation via embed."),
    MESSAGE_SEND_IN_THREADS(
            Permission.MESSAGE_SEND_IN_THREADS,
            "Send Messages in Threads",
            "Any confirmation message won't work in threads. For example reaction confirmation or reputation confirmation via embed"),
    MESSAGE_ATTACH_VOICE_MESSAGE(
            Permission.MESSAGE_ATTACH_VOICE_MESSAGE,
            "Send Messages in Voice Channel",
            "Any confirmation message won't work in threads. For example reaction confirmation or reputation confirmation via embed."),
    MESSAGE_EXT_EMOJI(
            Permission.MESSAGE_EXT_EMOJI, "Use External Emojis", "Reputation confirmation via emojis is not working."),
    BYPASS_SLOWMODE(
            Permission.BYPASS_SLOWMODE,
            "Bypass Slowmode",
            "Any confirmation message won't work from time to time. For example reaction confirmation or reputation confirmation via embed.");

    private final Permission permission;
    private final String permissionName;
    private final String restrictions;

    GeneralPermissions(Permission permission, String permissionName, String restrictions) {
        this.permission = permission;
        this.permissionName = permissionName;
        this.restrictions = restrictions;
    }

    public Permission permission() {
        return permission;
    }

    public boolean check(Function<Permission, Boolean> permissionCheck) {
        return permissionCheck.apply(permission);
    }

    public String problemMessage() {
        return "Permission `%s` is missing. %s".formatted(permissionName, restrictions);
    }
}
