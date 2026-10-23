/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler.autopost;

import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshInterval;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class AutopostEnableTest {

    @Test
    void parsesOptionsInAnyCase() {
        Assertions.assertEquals(
                Optional.of(RefreshInterval.DAILY), AutopostEnable.parseOption(RefreshInterval.class, "daily"));
        Assertions.assertEquals(
                Optional.of(RefreshInterval.WEEKLY), AutopostEnable.parseOption(RefreshInterval.class, "WEEKLY"));
        Assertions.assertEquals(
                Optional.of(RefreshType.DELETE_AND_REPOST),
                AutopostEnable.parseOption(RefreshType.class, "delete_and_repost"));
        Assertions.assertEquals(
                Optional.of(RefreshType.DELETE_AND_REPOST),
                AutopostEnable.parseOption(RefreshType.class, "Delete and Repost"));
    }

    @Test
    void unknownOptionsAreEmpty() {
        Assertions.assertEquals(Optional.empty(), AutopostEnable.parseOption(RefreshType.class, "sometimes"));
    }
}
