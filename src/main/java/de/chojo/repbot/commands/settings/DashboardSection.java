/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.settings;

/**
 * A section of the settings in the dashboard, with its path and the locale key of its name.
 */
public enum DashboardSection {
    GENERAL("settings/edit/general", "command.settings.section.general"),
    PROFILE("settings/edit/profile", "command.settings.section.profile"),
    RANKS("settings/edit/ranks", "command.settings.section.ranks"),
    REPUTATION("settings/edit/reputation", "command.settings.section.reputation"),
    CHANNELS("settings/edit/channels", "command.settings.section.channels"),
    ROLES("settings/edit/roles", "command.settings.section.roles"),
    REACTIONS("settings/edit/reactions", "command.settings.section.reactions"),
    THANKWORDS("settings/edit/thankwords", "command.settings.section.thankwords"),
    ABUSE_PROTECTION("settings/edit/abuse-protection", "command.settings.section.abuseprotection"),
    ANNOUNCEMENTS("settings/edit/announcements", "command.settings.section.announcements"),
    AUTOPOST("settings/edit/autopost", "command.settings.section.autopost"),
    LOG_CHANNEL("settings/edit/log-channel", "command.settings.section.logchannel");

    private final String path;
    private final String localeKey;

    DashboardSection(String path, String localeKey) {
        this.path = path;
        this.localeKey = localeKey;
    }

    /**
     * The path of the section, relative to the dashboard address.
     *
     * @return the path
     */
    public String path() {
        return path;
    }

    /**
     * The locale key of the section name.
     *
     * @return the locale key
     */
    public String localeKey() {
        return localeKey;
    }
}
