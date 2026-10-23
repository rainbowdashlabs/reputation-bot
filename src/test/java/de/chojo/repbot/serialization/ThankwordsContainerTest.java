/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.serialization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ThankwordsContainerTest {

    @Test
    void load() {
        Assertions.assertDoesNotThrow(ThankwordsContainer::load);
    }
}
