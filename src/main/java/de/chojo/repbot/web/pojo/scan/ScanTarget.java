/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.scan;

import net.dv8tion.jda.api.entities.channel.ChannelType;

public enum ScanTarget {
    TEXT,
    GUILD,
    FORUM,
    VOICE,
    THREAD,
    CATEGORY;

    public static ScanTarget fromChannelType(ChannelType type) {
        return switch (type) {
            case TEXT, NEWS -> TEXT;
            case VOICE, STAGE -> VOICE;
            case CATEGORY -> CATEGORY;
            case GUILD_PRIVATE_THREAD, GUILD_PUBLIC_THREAD, GUILD_NEWS_THREAD -> THREAD;
            case MEDIA, FORUM -> FORUM;
            default -> throw new IllegalArgumentException("Invalid channel target: " + type);
        };
    }
}
