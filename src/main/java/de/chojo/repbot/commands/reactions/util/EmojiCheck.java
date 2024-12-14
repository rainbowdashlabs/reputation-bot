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

/**
 * Utility class for checking and validating emojis in messages.
 */
public final class EmojiCheck {
    /**
     * Private constructor to prevent instantiation.
     */
    private EmojiCheck() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Checks the provided emoji in the context of the given message.
     *
     * @param message the message containing the emoji
     * @param emote   the emoji to check
     * @return the result of the emoji check
     */
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
            return new EmojiCheckResult(emoteByName.get().getAsMention(), emoteByName.get()
                                                                                     .getId(), CheckResult.EMOTE_FOUND);
        }

        // Check for unicode
        try {
            message.addReaction(Emoji.fromUnicode(emote)).complete();
        } catch (ErrorResponseException e) {
            return new EmojiCheckResult(null, "", CheckResult.UNKNOWN_EMOJI);
        }
        return new EmojiCheckResult(emote, "", CheckResult.EMOJI_FOUND);
    }

    /**
     * Checks if the given custom emoji can be used in the context of the provided message.
     *
     * @param emote   the custom emoji to check
     * @param message the message containing the emoji
     * @return true if the emoji can be used, false otherwise
     */
    private static boolean canUse(RichCustomEmoji emote, Message message) {
        if (emote == null) {
            return false;
        }
        try {
            message.addReaction(emote).queue();
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
