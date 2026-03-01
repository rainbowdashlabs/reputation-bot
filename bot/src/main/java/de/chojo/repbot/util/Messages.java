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

import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public final class Messages {
    private static final Logger log = getLogger(Messages.class);
    private static final Set<ErrorResponse> IGNORE_ERRORS = Set.of(
            ErrorResponse.UNKNOWN_MESSAGE,
            ErrorResponse.TOO_MANY_REACTIONS,
            ErrorResponse.REACTION_BLOCKED,
            ErrorResponse.UNKNOWN_CHANNEL,
            ErrorResponse.THREAD_LOCKED);

    private Messages() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static void markMessage(Message message, @Nullable Message refMessage, Settings settings) {
        var reaction = settings.thanking().reactions().mainReaction();
        if (settings.thanking().reactions().reactionIsEmote()) {
            message.getGuild()
                    .retrieveEmojiById(reaction)
                    .queue(
                            e -> {
                                markMessage(message, e, settings);
                                if (refMessage != null) markMessage(refMessage, e, settings);
                            },
                            err -> log.error("Could not resolve emoji.", err));
        } else {
            if (refMessage != null) markMessage(refMessage, reaction, settings);
            markMessage(message, reaction, settings);
        }
    }

    private static void markMessage(Message message, Emoji emote, Settings settings) {
        if (PermissionUtil.checkPermission(
                message.getGuildChannel().getPermissionContainer(),
                message.getGuild().getSelfMember(),
                Permission.MESSAGE_ADD_REACTION)) {
            handleMark(message.addReaction(emote), settings);
        }
    }

    private static void markMessage(Message message, String emoji, Settings settings) {
        if (PermissionUtil.checkPermission(
                message.getGuildChannel().getPermissionContainer(),
                message.getGuild().getSelfMember(),
                Permission.MESSAGE_ADD_REACTION)) {
            handleMark(message.addReaction(Emoji.fromUnicode(emoji)), settings);
        }
    }

    private static void handleMark(RestAction<Void> action, Settings settings) {
        action.queue(RestAction.getDefaultSuccess(), err -> {
            if (err instanceof ErrorResponseException e) {
                if (IGNORE_ERRORS.contains(e.getErrorResponse())) {
                    return;
                }
                if (e.getErrorResponse() == ErrorResponse.UNKNOWN_EMOJI) {
                    settings.thanking().reactions().mainReaction(null);
                }
            }
        });
    }
}
