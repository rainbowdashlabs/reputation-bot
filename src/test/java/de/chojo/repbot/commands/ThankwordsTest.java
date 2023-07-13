/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands;

import de.chojo.repbot.commands.thankwords.Thankwords;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ThankwordsTest {

    @Test
    void loadContainer() {
        Assertions.assertDoesNotThrow(Thankwords::loadContainer);
    }
}
