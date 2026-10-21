/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

/**
 * Request to set the custom name of a command. An empty alias resets it.
 *
 * @param command name of the command
 * @param alias   new name of the command
 */
public record CommandAliasPOJO(String command, String alias) {}
