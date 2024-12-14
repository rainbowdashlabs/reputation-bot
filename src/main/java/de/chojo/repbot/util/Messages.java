/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import de.chojo.repbot.dao.access.guild.settings.Settings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Utility class for handling message reactions.
 */
public final class Messages {
    private static final Logger log = getLogger(Messages.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private Messages() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Adds a reaction to a message based on the settings.
     *
     * @param message the message to add a reaction to
     * @param refMessage an optional reference message to also add the reaction to
     * @param settings the settings containing the reaction information
     */
    public static void markMessage(Message message, @Nullable Message refMessage, Settings settings) {
        var reaction = settings.thanking().reactions().mainReaction();
        if (settings.thanking().reactions().reactionIsEmote()) {
            message.getGuild().retrieveEmojiById(reaction).queue(e -> {
                markMessage(message, e);
                if (refMessage != null) markMessage(refMessage, e);
            }, err -> log.error("Could not resolve emoji.", err));
        } else {
            if (refMessage != null) markMessage(refMessage, reaction);
            markMessage(message, reaction);
        }
    }

    /**
     * Adds an emoji reaction to a message.
     *
     * @param message the message to add a reaction to
     * @param emote the emoji to add as a reaction
     */
    public static void markMessage(Message message, Emoji emote) {
        if (PermissionUtil.checkPermission(message.getGuildChannel().getPermissionContainer(), message.getGuild()
                                                                                                      .getSelfMember(), Permission.MESSAGE_ADD_REACTION)) {
            handleMark(message.addReaction(emote));
        }
    }

    /**
     * Adds a Unicode emoji reaction to a message.
     *
     * @param message the message to add a reaction to
     * @param emoji the Unicode emoji to add as a reaction
     */
    public static void markMessage(Message message, String emoji) {
        if (PermissionUtil.checkPermission(message.getGuildChannel().getPermissionContainer(), message.getGuild()
                                                                                                      .getSelfMember(), Permission.MESSAGE_ADD_REACTION)) {
            handleMark(message.addReaction(Emoji.fromUnicode(emoji)));
        }
    }

    /**
     * Handles the reaction addition action, ignoring specific error responses.
     *
     * @param action the RestAction to handle
     */
    private static void handleMark(RestAction<Void> action) {
        action.queue(RestAction.getDefaultSuccess(),
                ErrorResponseException.ignore(
                        ErrorResponse.UNKNOWN_MESSAGE,
                        ErrorResponse.TOO_MANY_REACTIONS,
                        ErrorResponse.REACTION_BLOCKED,
                        ErrorResponse.UNKNOWN_CHANNEL));
    }
}
