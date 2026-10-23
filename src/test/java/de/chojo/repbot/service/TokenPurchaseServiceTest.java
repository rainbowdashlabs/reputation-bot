/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.web.config.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TokenPurchaseServiceTest {

    @Test
    void everyoneMayPurchaseWhenAllowed() {
        Assertions.assertTrue(TokenPurchaseService.mayPurchase(true, Role.GUILD_USER));
        Assertions.assertTrue(TokenPurchaseService.mayPurchase(true, Role.GUILD_ADMIN));
    }

    @Test
    void onlyAdminsMayPurchaseOtherwise() {
        Assertions.assertFalse(TokenPurchaseService.mayPurchase(false, Role.GUILD_USER));
        Assertions.assertTrue(TokenPurchaseService.mayPurchase(false, Role.GUILD_ADMIN));
    }
}
