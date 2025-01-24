/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for filtering text channels in a guild based on permissions.
 */
public final class FilterUtil {

    /**
     * Private constructor to prevent instantiation.
     * Throws an UnsupportedOperationException if called.
     */
    private FilterUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Retrieves all text channels in the guild where the bot user has both read and write permissions.
     *
     * @param guild the guild to filter channels from
     * @return a list of accessible text channels
     */
    public static List<TextChannel> getAccessableTextChannel(Guild guild) {
        return filterChannelByPermission(guild, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
    }

    /**
     * Filters the text channels in the guild based on the specified permissions.
     *
     * @param guild the guild to filter channels from
     * @param permissions the permissions to check for each channel
     * @return a list of text channels that the bot user has the specified permissions for
     */
    public static List<TextChannel> filterChannelByPermission(Guild guild, Permission... permissions) {
        var self = guild.getSelfMember();
        return guild.getTextChannels().stream().filter(textChannel -> self.hasPermission(textChannel, permissions))
                    .collect(Collectors.toList());
    }
}
