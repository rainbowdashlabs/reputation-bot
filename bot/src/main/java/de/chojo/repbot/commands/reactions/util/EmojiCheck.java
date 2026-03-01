/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reactions.util;

import de.chojo.jdautil.parsing.Verifier;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public final class EmojiCheck {
    private EmojiCheck() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static EmojiCheckResult checkEmoji(Message message, String emote) {
        // Check for emote id
        if (Verifier.isValidId(emote)) {
            var emoteById = message.getGuild()
                    .retrieveEmojiById(Verifier.getIdRaw(emote).get())
                    .onErrorMap(err -> null)
                    .complete();
            if (!canUse(emoteById, message)) {
                return new EmojiCheckResult("", "", CheckResult.NOT_FOUND);
            }
            return new EmojiCheckResult(emoteById.getAsMention(), emoteById.getId(), CheckResult.EMOTE_FOUND);
        }

        // Check for name
        var emoteByName = message.getGuild().retrieveEmojis().complete().stream()
                .filter(e -> e.getName().equals(emote))
                .findFirst();

        if (emoteByName.isPresent()) {
            if (!canUse(emoteByName.get(), message)) {
                return new EmojiCheckResult("", "", CheckResult.NOT_FOUND);
            }
            return new EmojiCheckResult(
                    emoteByName.get().getAsMention(), emoteByName.get().getId(), CheckResult.EMOTE_FOUND);
        }

        // check for unicode
        try {
            message.addReaction(Emoji.fromUnicode(emote)).complete();
        } catch (ErrorResponseException e) {

            return new EmojiCheckResult(null, "", CheckResult.UNKNOWN_EMOJI);
        }
        return new EmojiCheckResult(emote, "", CheckResult.EMOJI_FOUND);
    }

    private static boolean canUse(RichCustomEmoji emote, Message message) {
        if (emote == null) {
            return false;
        }
        try {
            message.addReaction(emote).complete();
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
