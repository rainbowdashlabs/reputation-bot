/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.repbot.analyzer.results.match.ThankType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MessagesTest {

    private static Messages messages(
            boolean commandReputationEphemeral,
            boolean announceReaction,
            boolean announceAnswer,
            boolean announceMention,
            boolean announceFuzzy,
            boolean announceEmbed,
            boolean announceDirect,
            boolean announceCommand) {
        return new Messages(
                null,
                commandReputationEphemeral,
                announceReaction,
                announceAnswer,
                announceMention,
                announceFuzzy,
                announceEmbed,
                announceDirect,
                announceCommand,
                false);
    }

    private static Messages nothingAnnounced() {
        return messages(false, false, false, false, false, false, false, false);
    }

    @Test
    void nothingIsAnnouncedWhenAllSwitchesAreOff() {
        var messages = nothingAnnounced();
        for (var type : ThankType.values()) {
            Assertions.assertFalse(messages.isAnnounced(type), "Type " + type + " should not be announced");
        }
    }

    @Test
    void everyTypeIsAnnouncedIndependently() {
        Assertions.assertTrue(
                messages(false, true, false, false, false, false, false, false).isAnnounced(ThankType.REACTION));
        Assertions.assertTrue(
                messages(false, false, true, false, false, false, false, false).isAnnounced(ThankType.ANSWER));
        Assertions.assertTrue(
                messages(false, false, false, true, false, false, false, false).isAnnounced(ThankType.MENTION));
        Assertions.assertTrue(
                messages(false, false, false, false, true, false, false, false).isAnnounced(ThankType.FUZZY));
        Assertions.assertTrue(
                messages(false, false, false, false, false, true, false, false).isAnnounced(ThankType.EMBED));
        Assertions.assertTrue(
                messages(false, false, false, false, false, false, true, false).isAnnounced(ThankType.DIRECT));
    }

    @Test
    void enablingOneTypeDoesNotAnnounceAnother() {
        var messages = messages(false, true, false, false, false, false, false, false);
        Assertions.assertTrue(messages.isAnnounced(ThankType.REACTION));
        Assertions.assertFalse(messages.isAnnounced(ThankType.ANSWER));
        Assertions.assertFalse(messages.isAnnounced(ThankType.MENTION));
        Assertions.assertFalse(messages.isAnnounced(ThankType.FUZZY));
        Assertions.assertFalse(messages.isAnnounced(ThankType.EMBED));
        Assertions.assertFalse(messages.isAnnounced(ThankType.DIRECT));
        Assertions.assertFalse(messages.isAnnounced(ThankType.COMMAND));
    }

    /**
     * A command reply everyone can see is the announcement itself, so no additional message may be sent for it.
     */
    @Test
    void commandIsOnlyAnnouncedNextToAnEphemeralReply() {
        Assertions.assertTrue(
                messages(true, false, false, false, false, false, false, true).isAnnounced(ThankType.COMMAND));
        Assertions.assertFalse(
                messages(false, false, false, false, false, false, false, true).isAnnounced(ThankType.COMMAND));
        Assertions.assertFalse(
                messages(true, false, false, false, false, false, false, false).isAnnounced(ThankType.COMMAND));
        Assertions.assertFalse(
                messages(false, false, false, false, false, false, false, false).isAnnounced(ThankType.COMMAND));
    }

    /**
     * Exactly one of both may be true: either the visible reply announces the reputation, or an extra message does.
     */
    @Test
    void commandReplyAndExtraMessageNeverAnnounceTogether() {
        var publicReply = messages(false, false, false, false, false, false, false, true);
        Assertions.assertTrue(publicReply.isAnnouncedInReply());
        Assertions.assertFalse(publicReply.isAnnounced(ThankType.COMMAND));

        var hiddenReply = messages(true, false, false, false, false, false, false, true);
        Assertions.assertFalse(hiddenReply.isAnnouncedInReply());
        Assertions.assertTrue(hiddenReply.isAnnounced(ThankType.COMMAND));

        var announcementOff = messages(false, false, false, false, false, false, false, false);
        Assertions.assertFalse(announcementOff.isAnnouncedInReply());
        Assertions.assertFalse(announcementOff.isAnnounced(ThankType.COMMAND));
    }

    /**
     * A guild which never touched a message setting has no database row and is served by these defaults. They have to
     * match the column defaults of the message_states table, so that the bot does not send messages it did not send
     * before the announcements were introduced.
     */
    @Test
    void defaultsOnlyAnnounceReactionsAsTheReactionConfirmationDidBefore() {
        var messages = new Messages(null);

        Assertions.assertTrue(messages.isAnnounced(ThankType.REACTION));
        Assertions.assertTrue(messages.isAnnounceDelete());
        Assertions.assertFalse(messages.isCommandReputationEphemeral());
        for (var type : ThankType.values()) {
            if (type == ThankType.REACTION) continue;
            Assertions.assertFalse(messages.isAnnounced(type), "Type " + type + " should not be announced by default");
        }
    }
}
