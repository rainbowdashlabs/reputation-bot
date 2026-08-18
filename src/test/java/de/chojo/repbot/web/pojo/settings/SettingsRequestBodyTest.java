/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings;

import de.chojo.repbot.core.Web;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.web.pojo.settings.sub.GeneralPOJO;
import de.chojo.repbot.web.pojo.settings.sub.MessagesPOJO;
import de.chojo.repbot.web.pojo.settings.sub.ProfilePOJO;
import de.chojo.repbot.web.pojo.settings.sub.ReputationPOJO;
import de.chojo.repbot.web.pojo.settings.sub.ThankingPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ChannelsPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ReactionsPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.RolesHolderPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ThankwordsPOJO;
import io.javalin.json.JsonMapper;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

/**
 * Every settings route accepts its whole settings object as a request body. These objects are only ever built through
 * their constructor, so without an explicit creator jackson can not construct them at all and the route fails for
 * every body. Sending each object through the mapper of the api keeps that from happening again, and verifies that the
 * names it writes are the names it reads.
 */
class SettingsRequestBodyTest {
    private static final JsonMapper MAPPER = Web.jacksonMapper();

    private static <T> T roundTrip(T value, Class<T> type) {
        return MAPPER.fromJsonString(MAPPER.toJsonString(value, type), type);
    }

    @Test
    void messages() {
        var messages = roundTrip(
                new MessagesPOJO(true, true, false, true, false, true, false, true, false), MessagesPOJO.class);

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

    @Test
    void reputation() {
        var reputation =
                roundTrip(new ReputationPOJO(true, false, true, false, true, false, true), ReputationPOJO.class);

        Assertions.assertTrue(reputation.isReactionActive());
        Assertions.assertFalse(reputation.isAnswerActive());
        Assertions.assertTrue(reputation.isMentionActive());
        Assertions.assertFalse(reputation.isFuzzyActive());
        Assertions.assertTrue(reputation.isEmbedActive());
        Assertions.assertFalse(reputation.isDirectActive());
        Assertions.assertTrue(reputation.isCommandActive());
    }

    @Test
    void general() {
        var resetDate = Instant.parse("2026-01-02T03:04:05Z");
        var general = roundTrip(
                new GeneralPOJO(true, DiscordLocale.GERMAN, ReputationMode.WEEK, resetDate, 4711L, true),
                GeneralPOJO.class);

        Assertions.assertTrue(general.isStackRoles());
        Assertions.assertEquals(DiscordLocale.GERMAN, general.language().orElseThrow());
        Assertions.assertEquals(ReputationMode.WEEK, general.reputationMode());
        Assertions.assertEquals(resetDate, general.resetDate());
        Assertions.assertEquals(4711L, general.systemChannel());
        Assertions.assertTrue(general.everyoneTokenPurchase());
    }

    @Test
    void profile() {
        var profile = roundTrip(new ProfilePOJO("Nick", "https://example.invalid/pic.png", "Karma"), ProfilePOJO.class);

        Assertions.assertEquals("Nick", profile.nickname());
        Assertions.assertEquals("https://example.invalid/pic.png", profile.profilePictureUrl());
        Assertions.assertEquals("Karma", profile.reputationName());
    }

    @Test
    void channels() {
        var channels = roundTrip(new ChannelsPOJO(Set.of(1L, 2L), Set.of(3L), true), ChannelsPOJO.class);

        Assertions.assertEquals(Set.of(1L, 2L), channels.channelIds());
        Assertions.assertEquals(Set.of(3L), channels.categoryIds());
        Assertions.assertTrue(channels.isWhitelist());
    }

    @Test
    void reactions() {
        var reactions = roundTrip(new ReactionsPOJO(Set.of("star", "heart"), "star"), ReactionsPOJO.class);

        Assertions.assertEquals(Set.of("star", "heart"), reactions.reactions());
        Assertions.assertEquals("star", reactions.mainReaction());
    }

    @Test
    void thankwords() {
        var thankwords = roundTrip(new ThankwordsPOJO(Set.of("thanks", "danke")), ThankwordsPOJO.class);

        Assertions.assertEquals(Set.of("thanks", "danke"), thankwords.words());
    }

    @Test
    void thanking() {
        var thanking = roundTrip(
                new ThankingPOJO(
                        new ChannelsPOJO(Set.of(1L), Set.of(2L), false),
                        new RolesHolderPOJO(Set.of(10L)),
                        new RolesHolderPOJO(Set.of(11L)),
                        new RolesHolderPOJO(Set.of(12L)),
                        new RolesHolderPOJO(Set.of(13L)),
                        new ReactionsPOJO(Set.of("star"), "star"),
                        new ThankwordsPOJO(Set.of("thanks"))),
                ThankingPOJO.class);

        Assertions.assertEquals(Set.of(1L), thanking.channels().channelIds());
        Assertions.assertEquals(Set.of(10L), thanking.donorRoles().roleIds());
        Assertions.assertEquals(Set.of(11L), thanking.denyDonorRoles().roleIds());
        Assertions.assertEquals(Set.of(12L), thanking.receiverRoles().roleIds());
        Assertions.assertEquals(Set.of(13L), thanking.denyReceiverRoles().roleIds());
        Assertions.assertEquals("star", thanking.reactions().mainReaction());
        Assertions.assertEquals(Set.of("thanks"), thanking.thankwords().words());
    }

    /**
     * The settings are applied by difference, so an incomplete body would reset the omitted values instead of leaving
     * them alone. It has to be rejected.
     */
    @Test
    void incompleteBodyIsRejected() {
        Assertions.assertThrows(
                Exception.class, () -> MAPPER.fromJsonString("{\"announceAnswer\": true}", MessagesPOJO.class));
        Assertions.assertThrows(
                Exception.class, () -> MAPPER.fromJsonString("{\"reactionActive\": true}", ReputationPOJO.class));
    }
}
