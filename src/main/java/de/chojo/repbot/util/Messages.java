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

public final class Messages {
    private static final Logger log = getLogger(Messages.class);

    private Messages() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

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

    public static void markMessage(Message message, Emoji emote) {
        if (PermissionUtil.checkPermission(message.getGuildChannel().getPermissionContainer(), message.getGuild()
                                                                                                      .getSelfMember(), Permission.MESSAGE_ADD_REACTION)) {
            handleMark(message.addReaction(emote));
        }
    }

    public static void markMessage(Message message, String emoji) {
        if (PermissionUtil.checkPermission(message.getGuildChannel().getPermissionContainer(), message.getGuild()
                                                                                                      .getSelfMember(), Permission.MESSAGE_ADD_REACTION)) {
            handleMark(message.addReaction(Emoji.fromUnicode(emoji)));
        }
    }

    private static void handleMark(RestAction<Void> action) {
        action.queue(RestAction.getDefaultSuccess(),
                ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.TOO_MANY_REACTIONS));
    }
}
