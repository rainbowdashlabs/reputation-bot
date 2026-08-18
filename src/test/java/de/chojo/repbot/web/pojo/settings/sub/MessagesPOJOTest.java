/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import de.chojo.repbot.core.Web;
import io.javalin.json.JsonMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MessagesPOJOTest {
    private static final String FULL_BODY = """
            {
              "commandReputationEphemeral": true,
              "announceReaction": true,
              "announceAnswer": false,
              "announceMention": true,
              "announceFuzzy": false,
              "announceEmbed": true,
              "announceDirect": false,
              "announceCommand": true,
              "announceDelete": false
            }""";

    private static final JsonMapper MAPPER = Web.jacksonMapper();

    @Test
    void deserializesACompleteBody() {
        MessagesPOJO messages = MAPPER.fromJsonString(FULL_BODY, MessagesPOJO.class);

        Assertions.assertTrue(messages.isCommandReputationEphemeral());
        Assertions.assertTrue(messages.isAnnounceReaction());
        Assertions.assertFalse(messages.isAnnounceAnswer());
        Assertions.assertTrue(messages.isAnnounceMention());
        Assertions.assertFalse(messages.isAnnounceFuzzy());
        Assertions.assertTrue(messages.isAnnounceEmbed());
        Assertions.assertFalse(messages.isAnnounceDirect());
        Assertions.assertTrue(messages.isAnnounceCommand());
        Assertions.assertFalse(messages.isAnnounceDelete());
    }

    /**
     * The settings are applied by difference, so an incomplete body would reset the omitted switches instead of
     * leaving them alone. It has to be rejected.
     */
    @Test
    void rejectsAnIncompleteBody() {
        Assertions.assertThrows(
                Exception.class, () -> MAPPER.fromJsonString("{\"announceAnswer\": true}", MessagesPOJO.class));
    }
}
