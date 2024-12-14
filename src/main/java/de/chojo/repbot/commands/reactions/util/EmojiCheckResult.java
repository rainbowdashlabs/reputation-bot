/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reactions.util;

/**
 * Represents the result of an emoji check.
 *
 * @param mention the mention string
 * @param id the ID of the emoji
 * @param result the result of the check
 */
public record EmojiCheckResult(String mention, String id, CheckResult result) {
}
