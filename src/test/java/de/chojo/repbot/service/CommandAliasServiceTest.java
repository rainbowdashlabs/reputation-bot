/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandAliasServiceTest {

    private static Optional<String> validate(String command, String name) {
        return CommandAliasService.validate(command, name, false, Map.of());
    }

    @Test
    void acceptsValidNames() {
        assertEquals(Optional.empty(), validate("reputation", "thanks"));
        assertEquals(Optional.empty(), validate("profile", "karma-points_2"));
        assertEquals(Optional.empty(), validate("profile", "ränge"));
        assertEquals(Optional.empty(), validate("profile", "a".repeat(32)));
    }

    @Test
    void acceptsResetToEmptyOrOwnName() {
        assertEquals(Optional.empty(), validate("reputation", ""));
        assertEquals(Optional.empty(), validate("reputation", "reputation"));
    }

    @Test
    void rejectsUnknownCommand() {
        assertTrue(validate("top", "best").isPresent());
    }

    @Test
    void rejectsInvalidNames() {
        assertTrue(validate("profile", "a".repeat(33)).isPresent());
        assertTrue(validate("profile", "two words").isPresent());
        assertTrue(validate("profile", "exclaim!").isPresent());
    }

    @Test
    void rejectsNamesOfBotCommands() {
        assertTrue(
                CommandAliasService.validate("profile", "top", true, Map.of()).isPresent());
    }

    @Test
    void rejectsAliasOfOtherCommand() {
        var aliases = Map.of("reputation", "thanks");
        assertTrue(CommandAliasService.validate("profile", "thanks", false, aliases)
                .isPresent());
        assertEquals(Optional.empty(), CommandAliasService.validate("reputation", "thanks", false, aliases));
    }
}
