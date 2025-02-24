/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.utils.PermissionUtil;

/**
 * Utility class for handling permission errors.
 */
public final class PermissionErrorHandler {
    private PermissionErrorHandler() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Handles an InsufficientPermissionException by sending a permission error message to the appropriate channel.
     *
     * @param permissionException the exception to handle
     * @param shardManager the shard manager
     * @param localizer the localization context
     * @param configuration the configuration
     */
    public static void handle(InsufficientPermissionException permissionException, ShardManager shardManager,
                              LocalizationContext localizer, Configuration configuration) {
        var permission = permissionException.getPermission();
        var guildById = shardManager.getGuildById(permissionException.getGuildId());
        var channel = (TextChannel) permissionException.getChannel(guildById.getJDA());
        if (channel == null) return;
        sendPermissionError(channel, permission, localizer, configuration);
    }

    /**
     * Handles an InsufficientPermissionException by sending a permission error message to the appropriate channel.
     *
     * @param permissionException the exception to handle
     * @param guild the guild
     * @param localizer the localization context
     * @param configuration the configuration
     */
    public static void handle(InsufficientPermissionException permissionException, Guild guild, LocalizationContext localizer,
                              Configuration configuration) {
        var permission = permissionException.getPermission();
        var channel = (TextChannel) permissionException.getChannel(guild.getJDA());
        if (channel == null) return;
        sendPermissionError(channel, permission, localizer, configuration);
    }

    /**
     * Handles an InsufficientPermissionException by sending a permission error message to the appropriate channel.
     *
     * @param permissionException the exception to handle
     * @param channel the channel
     * @param localizer the localization context
     * @param configuration the configuration
     */
    public static void handle(InsufficientPermissionException permissionException, GuildMessageChannel channel,
                              LocalizationContext localizer, Configuration configuration) {
        var permission = permissionException.getPermission();
        if (channel == null) return;
        sendPermissionError(channel, permission, localizer, configuration);
    }

    /**
     * Sends a permission error message to the specified channel.
     *
     * @param channel the channel to send the message to
     * @param permission the missing permission
     * @param localizer the localization context
     * @param configuration the configuration
     */
    public static void sendPermissionError(GuildMessageChannel channel, Permission permission, LocalizationContext localizer,
                                           Configuration configuration) {
        var guild = channel.getGuild();
        var errorMessage = localizer.localize("error.missingPermission",
                Replacement.create("PERM", permission.getName(), Format.BOLD));
        if (guild.getSelfMember().hasPermission(permission)) {
            errorMessage += "\n" + localizer.localize("error.missingPermissionChannel",
                    Replacement.createMention("CHANNEL", channel));
        } else {
            errorMessage += "\n" + localizer.localize("error.missingPermissionGuild");
        }
        if (permission != Permission.MESSAGE_SEND && permission != Permission.VIEW_CHANNEL
            && PermissionUtil.checkPermission(channel.getPermissionContainer(), channel.getGuild()
                                                                                       .getSelfMember(), Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL)) {
            channel.sendMessage(errorMessage).queue();
            return;
        }
        // botlists always have permission issues. We will ignore them and wont try to notify anyone...
        if (configuration.botlist().isBotlistGuild(guild.getIdLong())) return;

        var ownerId = guild.getOwnerIdLong();
        var finalErrorMessage = errorMessage;
        guild.retrieveMemberById(ownerId)
             .flatMap(member -> member.getUser().openPrivateChannel())
             .flatMap(privateChannel -> privateChannel.sendMessage(finalErrorMessage))
             .onErrorMap(t -> null)
             .queue();
    }

    /**
     * Assert that the user has permissions.
     *
     * @param channel     channel to check
     * @param permissions permissions to check
     * @throws InsufficientPermissionException when the bot user doesnt have a permission
     */
    public static void assertPermissions(GuildMessageChannel channel, Permission... permissions) throws InsufficientPermissionException {
        var self = channel.getGuild().getSelfMember();
        for (var permission : permissions) {
            if (!self.hasPermission(channel, permission)) {
                throw new InsufficientPermissionException(channel, permission);
            }
        }
    }

    /**
     * Assert that the user has permissions.
     *
     * @param guild       guild to check
     * @param permissions permissions to check
     * @throws InsufficientPermissionException when the bot user doesnt have a permission
     */
    public static void assertPermissions(Guild guild, Permission... permissions) throws InsufficientPermissionException {
        var self = guild.getSelfMember();
        for (var permission : permissions) {
            if (!self.hasPermission(permission)) {
                throw new InsufficientPermissionException(guild, permission);
            }
        }
    }

    /**
     * Checks if the self user has the permissions in this channel and sends a permission error if one is missing.
     *
     * @param channel       channel to check
     * @param localizer     localizer
     * @param configuration configuration
     * @param permissions   permissions to check
     * @return true if a permission was missing and a message was sent
     */
    public static boolean assertAndHandle(GuildMessageChannel channel, LocalizationContext localizer, Configuration configuration, Permission... permissions) {
        try {
            assertPermissions(channel, permissions);
        } catch (InsufficientPermissionException e) {
            handle(e, channel, localizer, configuration);
            return true;
        }
        return false;
    }
}
